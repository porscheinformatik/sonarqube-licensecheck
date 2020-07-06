package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.StringUtils;

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

@Slf4j
public class GradleDependencyScanner implements Scanner {

    private final MavenLicenseService mavenLicenseService;

    public GradleDependencyScanner(MavenLicenseService mavenLicenseService) {
        this.mavenLicenseService = mavenLicenseService;
    }

    @Override
    public Set<Dependency> scan(File moduleDir) {
        Map<Pattern, String> defaultLicenseMap = mavenLicenseService.getLicenseMap();
        Set<Dependency> tmpSet = readLicenseDetailsJson(moduleDir.getPath());
        Set<Dependency> finalSet = tmpSet.stream()
            .map(d -> mapMavenDependencyToLicense(defaultLicenseMap, d))
            .collect(Collectors.toSet());
        return finalSet;
    }

    private Set<Dependency> readLicenseDetailsJson(String moduleDirPath) {
        String filePath =
            moduleDirPath + File.separator + "build" + File.separator + "reports" + File.separator
                + "dependency-license" + File.separator + "license-details.json";
        Set<Dependency> dependencySet = new HashSet<>();
        try (InputStream fis = new FileInputStream(filePath);
             JsonReader jsonReader = Json.createReader(fis);) {
            JsonArray arr = jsonReader.readObject().getJsonArray("dependencies");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject jsonDepObj = arr.get(i).asJsonObject();
                JsonArray arrModuleUrls = jsonDepObj.getJsonArray("moduleUrls");
                JsonArray arrModuleLicenses = jsonDepObj.getJsonArray("moduleLicenses");
                String moduleLicenseUrl = null;
                String moduleLicense = null;

                if (arrModuleUrls != null) {
                    moduleLicenseUrl = arrModuleUrls.getString(0, null);
                }
                if (arrModuleLicenses != null) {
                    JsonObject firstJsonObj = arrModuleLicenses.getJsonObject(0);
                    if (firstJsonObj != null) {
                        moduleLicense = firstJsonObj.getString("moduleLicense", null);
                        if (moduleLicense == null) {
                            moduleLicense = firstJsonObj.getString("moduleLicenseUrl", null);
                        }
                    }
                }
                Dependency dep = new Dependency(
                    jsonDepObj.getString("moduleName", null),
                    jsonDepObj.getString("moduleVersion", null),
                    moduleLicense);
                dep.setPomPath(moduleLicenseUrl);
                dependencySet.add(dep);
            }
            return dependencySet;
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException " + e.getMessage());
        } catch (IOException e) {
            log.error("IOException " + e.getMessage());
        }
        return dependencySet;
    }

    private Dependency mapMavenDependencyToLicense(Map<Pattern, String> defaultLicenseMap, Dependency dependency) {
        if (StringUtils.isBlank(dependency.getLicense())) {
            log.error(" License not found for Dependency {}", dependency);
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

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class DefaultLicenseMap {
        private Pattern regex;
        private String license;
    }
}
