package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.codehaus.plexus.util.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradleDependencyScanner implements Scanner {

    private static final Logger LOGGER = Loggers.get(GradleDependencyScanner.class);
    private final MavenLicenseService mavenLicenseService;

    public GradleDependencyScanner(MavenLicenseService mavenLicenseService) {
        this.mavenLicenseService = mavenLicenseService;
    }

    @Override
    public Set<Dependency> scan(File moduleDir) {
        Map<Pattern, String> defaultLicenseMap = mavenLicenseService.getLicenseMap();
        String filePath = moduleDir.getPath() + File.separator + "build" + File.separator + "reports" + File.separator
            + "dependency-license" + File.separator + "license-details.json";
        Set<Dependency> tmpSet = readLicenseDetailsJson(filePath);
        Set<Dependency> finalSet = tmpSet.stream()
            .map(d -> mapMavenDependencyToLicense(defaultLicenseMap, d))
            .collect(Collectors.toSet());
        return finalSet;
    }

    private Set<Dependency> readLicenseDetailsJson(String filePath) {
        Set<Dependency> dependencySet = new HashSet<>();
        try (InputStream fis = new FileInputStream(filePath);
             JsonReader jsonReader = Json.createReader(fis);) {
            JsonArray arr = jsonReader.readObject().getJsonArray("dependencies");
            prepareDependencySet(dependencySet, arr);
            return dependencySet;
        } catch (FileNotFoundException e) {
            LOGGER.error("FileNotFoundException " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("IOException " + e.getMessage());
        }
        return dependencySet;
    }

    private void prepareDependencySet(Set<Dependency> dependencySet, JsonArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jsonDepObj = arr.get(i).asJsonObject();
            JsonArray arrModuleUrls = jsonDepObj.getJsonArray("moduleUrls");
            String moduleLicense = getModuleLicenseFromJsonObject(jsonDepObj);
            String moduleLicenseUrl = null;
            if (arrModuleUrls != null) {
                moduleLicenseUrl = arrModuleUrls.getString(0, null);
            }
            Dependency dep = new Dependency(jsonDepObj.getString("moduleName", null),
                jsonDepObj.getString("moduleVersion", null), moduleLicense);
            dep.setPomPath(moduleLicenseUrl);
            dependencySet.add(dep);
        }
    }

    private String getModuleLicenseFromJsonObject(JsonObject jsonDepObj) {
        String moduleLicense = null;
        JsonArray arrModuleLicenses = jsonDepObj.getJsonArray("moduleLicenses");
        if (arrModuleLicenses != null) {
            moduleLicense = getModuleLicense(arrModuleLicenses);
        }
        return moduleLicense;
    }

    private String getModuleLicense(JsonArray arrModuleLicenses) {
        String moduleLicense = null;
        JsonObject firstJsonObj = arrModuleLicenses.getJsonObject(0);
        if (firstJsonObj != null) {
            moduleLicense = firstJsonObj.getString("moduleLicense", null);
            if (moduleLicense == null) {
                moduleLicense = firstJsonObj.getString("moduleLicenseUrl", null);
            }
        }
        return moduleLicense;
    }

    private Dependency mapMavenDependencyToLicense(Map<Pattern, String> defaultLicenseMap, Dependency dependency) {
        if (StringUtils.isBlank(dependency.getLicense())) {
            LOGGER.error(" License not found for Dependency {}", dependency);
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
