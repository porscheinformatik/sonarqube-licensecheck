package at.porscheinformatik.sonarqube.licensecheck.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;

public class GradleDependencyScanner implements Scanner
{
    private static final Logger LOGGER = Loggers.get(GradleDependencyScanner.class);
    private final MavenLicenseService mavenLicenseService;

    public GradleDependencyScanner(MavenLicenseService mavenLicenseService)
    {
        this.mavenLicenseService = mavenLicenseService;
    }

    @Override
    public Set<Dependency> scan(File moduleDir)
    {
        Map<Pattern, String> defaultLicenseMap = mavenLicenseService.getLicenseMap();

        File licenseDetailsJsonFile = new File(moduleDir, "build" + File.separator + "reports" + File.separator
            + "dependency-license" + File.separator + "license-details.json");

        if (!licenseDetailsJsonFile.exists())
        {
            LOGGER.info("No license-details.json file found in {} - skipping Gradle dependency scan",
                licenseDetailsJsonFile.getPath());
            return Collections.emptySet();
        }

        return readLicenseDetailsJson(licenseDetailsJsonFile)
            .stream()
            .map(d -> mapMavenDependencyToLicense(defaultLicenseMap, d))
            .collect(Collectors.toSet());
    }

    private Set<Dependency> readLicenseDetailsJson(File licenseDetailsJsonFile)
    {
        Set<Dependency> dependencySet = new HashSet<>();
        try (InputStream fis = new FileInputStream(licenseDetailsJsonFile);
            JsonReader jsonReader = Json.createReader(fis))
        {
            JsonArray arr = jsonReader.readObject().getJsonArray("dependencies");
            prepareDependencySet(dependencySet, arr);
            return dependencySet;
        }
        catch (IOException e)
        {
            LOGGER.error("Problems reading Gradle license file {}: {}",
                licenseDetailsJsonFile.getPath(), e.getMessage());
        }
        return dependencySet;
    }

    private void prepareDependencySet(Set<Dependency> dependencySet, JsonArray arr)
    {
        for (javax.json.JsonValue entry : arr)
        {
            JsonObject jsonDepObj = entry.asJsonObject();
            JsonArray arrModuleUrls = jsonDepObj.getJsonArray("moduleUrls");
            String moduleLicense = getModuleLicenseFromJsonObject(jsonDepObj);
            String moduleLicenseUrl = null;
            if (arrModuleUrls != null)
            {
                moduleLicenseUrl = arrModuleUrls.getString(0, null);
            }
            Dependency dep = new Dependency(jsonDepObj.getString("moduleName", null),
                jsonDepObj.getString("moduleVersion", null), moduleLicense, LicenseCheckRulesDefinition.LANG_JAVA);
            dep.setPomPath(moduleLicenseUrl);
            dependencySet.add(dep);
        }
    }

    private String getModuleLicenseFromJsonObject(JsonObject jsonDepObj)
    {
        String moduleLicense = null;
        JsonArray arrModuleLicenses = jsonDepObj.getJsonArray("moduleLicenses");
        if (arrModuleLicenses != null)
        {
            moduleLicense = getModuleLicense(arrModuleLicenses);
        }
        return moduleLicense;
    }

    private String getModuleLicense(JsonArray arrModuleLicenses)
    {
        String moduleLicense = null;
        JsonObject firstJsonObj = arrModuleLicenses.getJsonObject(0);
        if (firstJsonObj != null)
        {
            moduleLicense = firstJsonObj.getString("moduleLicense", null);
            if (moduleLicense == null)
            {
                moduleLicense = firstJsonObj.getString("moduleLicenseUrl", null);
            }
        }
        return moduleLicense;
    }

    private Dependency mapMavenDependencyToLicense(Map<Pattern, String> defaultLicenseMap, Dependency dependency)
    {
        if (StringUtils.isBlank(dependency.getLicense()))
        {
            LOGGER.error(" License not found for Dependency {}", dependency);
            return dependency;
        }

        for (Map.Entry<Pattern, String> allowedDependency : defaultLicenseMap.entrySet())
        {
            if (allowedDependency.getKey().matcher(dependency.getLicense()).matches())
            {
                dependency.setLicense(allowedDependency.getValue());
                break;
            }
        }
        return dependency;
    }
}
