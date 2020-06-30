package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.StringUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
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
        Gson gson = new Gson();
        String filePath =
            moduleDir.getPath() + File.separator + "build" + File.separator + "reports" + File.separator
                + "dependency-license" + File.separator + "license-details.json";
        try {
            GradleLicenseDependency gradleLicenseDependency =
                gson.fromJson(new FileReader(filePath), GradleLicenseDependency.class);

            Map<Pattern, String> defaultLicenseMap = readDefaultLicenseMappingJsonFile();
            Set<Dependency> tmpSet = convert2Dependencies(gradleLicenseDependency);
            Set<Dependency> finalSet = tmpSet.stream()
                .map(this.loadLicenseFromPom(defaultLicenseMap)).collect(Collectors.toSet());
            finalSet.forEach(System.out::println);
            return finalSet;
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
        return null;
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

    private Set<Dependency> convert2Dependencies(GradleLicenseDependency gradleLicenseDependency) {
        List<GradleDependency> gDependencies = gradleLicenseDependency.getDependencies();
        return gDependencies.stream().map(this::convert2Dependency).collect(Collectors.toSet());
    }

    private Dependency convert2Dependency(GradleDependency gd) {
        Dependency d = new Dependency(gd.getModuleName(), gd.getModuleVersion(), gd.getModuleLicense());
        d.setPomPath(gd.getModuleLicenseUrl());
        return d;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class DefaultLicenseMap {
        private Pattern regex;
        private String license;
    }

    private Map<Pattern, String> readDefaultLicenseMappingJsonFile() {
        Map<Pattern, String> defaultLicenseMap = new HashMap<>();
        Path p = Paths.get("src", "main", "resources", "at", "porscheinformatik", "sonarqube",
            "licensecheck", "mavenlicense", "default_license_mapping.json");

        try (InputStream fis = new FileInputStream(p.toFile().getAbsolutePath());
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
}
