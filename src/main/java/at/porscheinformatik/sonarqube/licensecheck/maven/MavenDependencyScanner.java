package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;

public class MavenDependencyScanner implements Scanner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyScanner.class);

    private final MavenLicenseService mavenLicenseService;

    private final MavenDependencyService mavenDependencyService;

    public MavenDependencyScanner(MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService)
    {
        this.mavenLicenseService = mavenLicenseService;
        this.mavenDependencyService = mavenDependencyService;
    }

    @Override
    public List<Dependency> scan(File moduleDir, String mavenProjectDependencies)
    {
        JsonReader jsonReader = Json.createReader(new StringReader(mavenProjectDependencies));
        Set<Dependency> dependencies = new HashSet<>();

        parseDependencyJson(dependencies, jsonReader.readArray());

        loadLicenses(dependencies);

        mapAdditionalLicenses(dependencies);

        jsonReader.close();

        return new ArrayList<>(dependencies);
    }

    private void mapAdditionalLicenses(final Set<Dependency> dependencies)
    {
        for (Dependency dependency : dependencies)
        {
            if (StringUtils.isBlank(dependency.getLicense()))
            {
                for (MavenDependency allowedDependency : mavenDependencyService.getMavenDependencies())
                {
                    String matchString = allowedDependency.getKey();
                    if (dependency.getName().matches(matchString))
                    {
                        dependency.setLicense(allowedDependency.getLicense());
                    }
                }
            }
        }
    }

    private static void parseDependencyJson(Set<Dependency> dependencies, JsonArray jsonDependencyArray)
    {
        for (int i = 0; i < jsonDependencyArray.size(); i++)
        {
            JsonObject jsonDependency = jsonDependencyArray.getJsonObject(i);
            String scope = jsonDependency.getString("s");
            if ("compile".equals(scope) || "runtime".equals(scope))
            {
                if (jsonDependency.containsKey("d"))
                {
                    parseDependencyJson(dependencies, jsonDependency.getJsonArray("d"));
                }

                Dependency dependency =
                    new Dependency(jsonDependency.getString("k"), jsonDependency.getString("v"), null);
                dependencies.add(dependency);
            }
        }
    }

    private void loadLicenses(Set<Dependency> dependencies)
    {
        File mavenRepositoryDir = DirectoryFinder.getMavenRepsitoryDir();

        if (mavenRepositoryDir == null)
        {
            LOGGER.error("Could not find local Repository in settings.xml (user home, MAVEN_HOME).");
            return;
        }

        Map<Pattern, String> licenseMap = mavenLicenseService.getLicenseMap();

        for (Dependency dependency : dependencies)
        {
            List<License> licenses =
                LicenseFinder.getLicenses(DirectoryFinder.getPomPath(dependency, mavenRepositoryDir));

            if (!licenses.isEmpty())
            {
                outer: for (License license : licenses)
                {
                    String licenseName = license.getName();
                    if (StringUtils.isNotBlank(licenseName))
                    {
                        for (Entry<Pattern, String> entry : licenseMap.entrySet())
                        {
                            if (entry.getKey().matcher(licenseName).matches())
                            {
                                dependency.setLicense(entry.getValue());
                                break outer;
                            }
                        }
                    }
                    LOGGER.info("No licenses found for '{}'", licenseName);
                }
            }
            else
            {
                LOGGER.info("No licenses found in dependency {}", dependency.getName());
            }
        }
    }

}
