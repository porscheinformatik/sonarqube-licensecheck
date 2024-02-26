package at.porscheinformatik.sonarqube.licensecheck.nugetlicense;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;

public class NugetLicenseDependencyScanner implements Scanner
{
    private static final Logger LOGGER = Loggers.get(NugetLicenseDependencyScanner.class);

    private final LicenseMappingService licenseMappingService;

    public NugetLicenseDependencyScanner(LicenseMappingService licenseMappingService)
    {
        this.licenseMappingService = licenseMappingService;
    }

    @Override
    public Set<Dependency> scan(SensorContext context)
    {
        LOGGER.debug("Finding and scanning licenses.json");

        FileSystem fs = context.fileSystem();
        FilePredicate licenseJsonPredicate = fs.predicates().matchesPathPattern("**/licenses.json");

        Set<Dependency> allDependencies = new HashSet<>();

        for (InputFile licenseJsonFile : fs.inputFiles(licenseJsonPredicate))
        {
            context.markForPublishing(licenseJsonFile);
            LOGGER.info("Scanning for licenses (file={})", licenseJsonFile.toString());
            allDependencies.addAll(dependencyParser(licenseJsonFile));
        }

        LOGGER.debug("Nuget scanning complete.");

        return allDependencies;
    }

    private Set<Dependency> dependencyParser(InputFile licenseJsonFile)
    {
        Set<Dependency> dependencies = new HashSet<>();

        try (InputStream fis = licenseJsonFile.inputStream();
            JsonReader jsonReader = Json.createReader(fis))
        {
            JsonArray licensesJson = jsonReader.readArray();

            if (licensesJson != null)
            {
                for (int i = 0; i < licensesJson.size(); i++)
                {
                    JsonObject nextPackage = licensesJson.getJsonObject(i);
                    String packageName = nextPackage.getString("PackageName");
                    String packageVersion = nextPackage.getString("PackageVersion");
                    String packageLicense = nextPackage.getString("LicenseType");

                    if (dependencies.stream().anyMatch(d -> packageName.equals(d.getName()) && packageVersion.equals(d.getVersion())))
                    {
                        LOGGER.debug("Package {} {} has already been encountered and will not be scanned again", packageName, packageVersion);
                        continue;
                    }

                    String license = licenseMappingService.mapLicense(packageLicense);

                    LOGGER.debug("Found license. Name: {}  Version: {}  License: {}", packageName, packageVersion, packageLicense);

                    Dependency dependency = new Dependency(packageName, packageVersion, license, LicenseCheckRulesDefinition.LANG_CS);
                    dependency.setInputComponent(licenseJsonFile);
                    dependency.setTextRange(licenseJsonFile.selectLine(1));

                    dependencies.add(dependency);
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Error reading license.json", e);
        }

        return dependencies;
    }
}