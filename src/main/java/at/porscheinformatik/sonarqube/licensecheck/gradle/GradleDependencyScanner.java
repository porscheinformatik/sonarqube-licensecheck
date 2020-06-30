package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class GradleDependencyScanner implements Scanner {

    private final MavenDependencyService mavenDependencyService;

    public GradleDependencyScanner(MavenDependencyService mavenDependencyService) {
        this.mavenDependencyService = mavenDependencyService;
    }

    @Override
    public Set<Dependency> scan(File moduleDir) {
        Map<Pattern, String> defaultLicenseMap = readDefaultLicenseMappingJsonFile();
        Set<Dependency> tmpSet = readLicenseDetailsJson(moduleDir.getPath());
        Set<Dependency> finalSet = tmpSet.stream()
            .map(this.loadLicenseFromPom(defaultLicenseMap)).collect(Collectors.toSet());
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
                JsonObject jsoDepObj = arr.get(i).asJsonObject();
                Dependency dep = new Dependency(jsoDepObj.getString("moduleName"), jsoDepObj.getString("moduleVersion"),
                    jsoDepObj.getString("moduleLicense"));
                dep.setPomPath(jsoDepObj.getString("moduleLicenseUrl"));
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

    private Dependency mapMavenDependencyToLicense(Dependency dependency) {
        if (StringUtils.isNotBlank(dependency.getLicense())) {
            return dependency;
        }
        for (MavenDependency allowedDependency : mavenDependencyService.getMavenDependencies()) {
            String matchString = allowedDependency.getKey();
            if (dependency.getName().matches(matchString)) {
                dependency.setLicense(allowedDependency.getLicense());
            }
        }
        return dependency;
    }

    private Function<Dependency, Dependency> loadLicenseFromPom(Map<Pattern, String> licenseMap) {
        return (Dependency dependency) ->
        {
            return licenseMatcher(licenseMap, dependency);
        };
    }

    private static Dependency licenseMatcher(Map<Pattern, String> licenseMap, Dependency dependency) {
        String licenseName = dependency.getLicense();
        if (StringUtils.isBlank(licenseName)) {
            log.info("Dependency '{}' has no license set.", dependency.getName());
        }
        for (Map.Entry<Pattern, String> entry : licenseMap.entrySet()) {
            if (entry.getKey().matcher(licenseName).matches()) {
                dependency.setLicense(entry.getValue());
            }
        }
        log.info("No licenses found for '{}'", licenseName);
        return dependency;
    }

    private Map<Pattern, String> readDefaultLicenseMappingJsonFile() {
        Map<Pattern, String> defaultLicenseMap = new HashMap<>();
        try (InputStream fis = MavenLicenseSettingsService.class.getResourceAsStream("default_license_mapping.json");
             JsonReader jsonReader = Json.createReader(fis)) {
            JsonArray arr = jsonReader.readArray();
            for (int i = 0; i < arr.size(); i++) {
                String regex = arr.get(i).asJsonObject().getString("regex");
                String license = arr.getJsonObject(i).getString("license");
                defaultLicenseMap.put(Pattern.compile(regex), license);
            }
            return defaultLicenseMap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
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
