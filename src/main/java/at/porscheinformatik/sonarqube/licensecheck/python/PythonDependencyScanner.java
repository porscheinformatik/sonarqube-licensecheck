package at.porscheinformatik.sonarqube.licensecheck.python;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.codehaus.plexus.util.StringUtils;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PythonDependencyScanner implements Scanner {

    private static final Logger LOGGER = Loggers.get(PythonDependencyScanner.class);
    private final LicenseMappingService licenseMappingService;

    public PythonDependencyScanner(LicenseMappingService licenseMappingService) {
        this.licenseMappingService = licenseMappingService;
    }

    @Override
    public Set<Dependency> scan(SensorContext context) {
        File moduleDir = context.fileSystem().baseDir();
        Map<Pattern, String> defaultLicenseMap = licenseMappingService.getLicenseMap();
        File licenseDetailsJsonFile = new File(
            moduleDir,
            "build" +
            File.separator +
            "reports" +
            File.separator +
            "dependency-license" +
            File.separator +
            "license-details.json"
        );
        if (!licenseDetailsJsonFile.exists()) {
            LOGGER.info(
                "No license-details.json file found in {} - skipping Python dependency scan",
                licenseDetailsJsonFile.getPath()
            );
            return Collections.emptySet();
        }
        return readLicenseDetailsJson(licenseDetailsJsonFile)
            .stream()
            .map(d -> mapDependencyToLicense(defaultLicenseMap, d))
            .peek(d -> d.setInputComponent(context.module()))
            .collect(Collectors.toSet());
    }

    private Set<Dependency> readLicenseDetailsJson(File licenseDetailsJsonFile) {
        final Set<Dependency> dependencySet = new HashSet<>();
        try (
            InputStream fis = new FileInputStream(licenseDetailsJsonFile);
            JsonReader jsonReader = Json.createReader(fis)
        ) {
            JsonObject jo = jsonReader.readObject();
            JsonArray arr = jo.getJsonArray("dependencies");
            prepareDependencySet(dependencySet, arr);
            return dependencySet;
        } catch (Exception e) {
            LOGGER.error(
                "Problems reading Python license file {}: {}",
                licenseDetailsJsonFile.getPath(),
                e.getMessage()
            );
        }
        return dependencySet;
    }

    private void prepareDependencySet(Set<Dependency> dependencySet, JsonArray arr) {
        for (javax.json.JsonValue entry : arr) {
            JsonObject jsonDepObj = entry.asJsonObject();
            String moduleLicense = getModuleLicenseFromJsonObject(jsonDepObj);
            String moduleLicenseUrl = null;
            if (jsonDepObj.containsKey("moduleUrls")) {
                JsonArray urls = jsonDepObj.getJsonArray("moduleUrls");
                if (urls != null && !urls.isEmpty()) {
                    moduleLicenseUrl = urls.getString(0, null);
                }
            }
            Dependency dep = new Dependency(
                jsonDepObj.getString("moduleName", null),
                jsonDepObj.getString("moduleVersion", null),
                moduleLicense,
                LicenseCheckRulesDefinition.LANG_PYTHON
            );
            dep.setPomPath(moduleLicenseUrl);
            dependencySet.add(dep);
        }
    }

    private String getModuleLicenseFromJsonObject(JsonObject jsonDepObj) {
        String moduleLicense = null;
        if (jsonDepObj.containsKey("moduleLicenses")) {
            JsonArray arrModuleLicenses = jsonDepObj.getJsonArray("moduleLicenses");
            if (arrModuleLicenses != null && !arrModuleLicenses.isEmpty()) {
                JsonObject firstJsonObj = arrModuleLicenses.getJsonObject(0);
                if (firstJsonObj != null) {
                    moduleLicense = firstJsonObj.getString("moduleLicense", null);
                    if (moduleLicense == null) {
                        moduleLicense = firstJsonObj.getString("moduleLicenseUrl", null);
                    }
                }
            }
        }
        return moduleLicense;
    }

    private Dependency mapDependencyToLicense(
        Map<Pattern, String> defaultLicenseMap,
        Dependency dependency
    ) {
        if (StringUtils.isBlank(dependency.getLicense())) {
            LOGGER.error("License not found for Dependency {}", dependency);
            return dependency;
        }
        for (Map.Entry<Pattern, String> allowedDependency : defaultLicenseMap.entrySet()) {
            if (allowedDependency.getKey().matcher(dependency.getLicense()).matches()) {
                dependency.setLicense(allowedDependency.getValue());
                break;
            }
        }
        return dependency;
    }
}
