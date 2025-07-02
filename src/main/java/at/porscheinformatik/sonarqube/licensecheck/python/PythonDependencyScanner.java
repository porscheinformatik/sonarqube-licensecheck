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
            "tests" + File.separator + "reports" + File.separator + "python-licenses.json"
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
            JsonArray arr = jo.getJsonArray("packages");
            if (arr != null) {
                prepareDependencySet(dependencySet, arr);
            }
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
            String license = jsonDepObj.getString("license", null);
            String name = jsonDepObj.getString("name", null);
            String version = jsonDepObj.getString("version", null);
            String url = jsonDepObj.getString("homePage", null);
            // If license is null or empty, fallback to homePage, then url
            if (license == null || license.isEmpty()) {
                license = url;
                if ((license == null || license.isEmpty()) && jsonDepObj.containsKey("url")) {
                    license = jsonDepObj.getString("url", null);
                }
            }
            Dependency dep = new Dependency(
                name,
                version,
                license,
                LicenseCheckRulesDefinition.LANG_PYTHON
            );
            dep.setPomPath(url);
            dependencySet.add(dep);
        }
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
