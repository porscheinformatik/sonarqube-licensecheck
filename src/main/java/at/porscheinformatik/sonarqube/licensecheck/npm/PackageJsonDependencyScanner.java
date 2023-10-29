package at.porscheinformatik.sonarqube.licensecheck.npm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

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

public class PackageJsonDependencyScanner implements Scanner
{
    private static final Logger LOGGER = Loggers.get(PackageJsonDependencyScanner.class);
    private static final String PACKAGE_LICENSE = "license";

    private final LicenseMappingService licenseMappingService;
    private final boolean resolveTransitiveDeps;

    public PackageJsonDependencyScanner(LicenseMappingService licenseMappingService, boolean resolveTransitiveDeps)
    {
        this.licenseMappingService = licenseMappingService;
        this.resolveTransitiveDeps = resolveTransitiveDeps;
    }

    @Override
    public Set<Dependency> scan(SensorContext context)
    {
        FileSystem fs = context.fileSystem();
        FilePredicate packageJsonPredicate = fs.predicates().matchesPathPattern("**/package.json");

        Set<Dependency> allDependencies = new HashSet<>();

        LOGGER.info("Scanning for NPM dependencies (dir={})", fs.baseDir());
        for (InputFile packageJsonFile : fs.inputFiles(packageJsonPredicate))
        {
            context.markForPublishing(packageJsonFile);

            LOGGER.info("Scanning package.json: (path={})", packageJsonFile);
            allDependencies.addAll(dependencyParser(fs.baseDir(), packageJsonFile));
        }

        return allDependencies;
    }

    private Set<Dependency> dependencyParser(File baseDir, InputFile packageJsonFile)
    {
        Set<Dependency> dependencies = new HashSet<>();

        try (InputStream fis = packageJsonFile.inputStream();
            JsonReader jsonReader = Json.createReader(fis))
        {
            JsonObject packageJson = jsonReader.readObject();

            JsonObject packageJsonDependencies = packageJson.getJsonObject("dependencies");
            if (packageJsonDependencies != null)
            {
                scanDependencies(new File(packageJsonFile.uri().resolve("node_modules")), packageJsonDependencies.keySet(), 
                    dependencies);
                dependencies.forEach(dependency ->
                {
                    dependency.setInputComponent(packageJsonFile);
                    dependency.setTextRange(packageJsonFile.newRange(1, 0, packageJsonFile.lines(), 0));
                });
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error reading package.json", e);
        }

        return dependencies;
    }

    private void scanDependencies(File nodeModulesDir, Set<String> packageNames, Set<Dependency> dependencies)
    {
        LOGGER.info("Scanning NPM packages " + packageNames);

        for (String packageName : packageNames)
        {
            if (dependencies.stream().anyMatch(d -> packageName.equals(d.getName())))
            {
                LOGGER.debug("Package {} has already been encountered and will not be scanned again", packageName);
                continue;
            }

            File packageJsonFile = new File(nodeModulesDir, packageName + "/package.json");
            if (!packageJsonFile.exists())
            {
                LOGGER.warn("No package.json file found for package {} at {} - skipping dependency.",
                        packageName, packageJsonFile);
                continue;
            }

            try (InputStream fis = new FileInputStream(packageJsonFile);
                JsonReader jsonReader = Json.createReader(fis))
            {
                JsonObject packageJson = jsonReader.readObject();
                if (packageJson != null)
                {
                    String license = licenseMappingService.mapLicense(readLicense(packageJson));

                    dependencies.add(new Dependency(packageName, packageJson.getString("version", null), license,
                        LicenseCheckRulesDefinition.LANG_JS));

                    if (resolveTransitiveDeps)
                    {
                        JsonObject packageJsonDependencies = packageJson.getJsonObject("dependencies");
                        if (packageJsonDependencies != null)
                        {
                            scanDependencies(nodeModulesDir, packageJsonDependencies.keySet(), dependencies);
                        }
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                LOGGER.error("Could not load package.json", e);
            }
            catch (Exception e)
            {
                LOGGER.error("Could not check NPM package " + packageName, e);
            }
        }
    }

    private String readLicense(JsonObject packageJson)
    {
        if (packageJson.containsKey(PACKAGE_LICENSE))
        {
            final Object licenceObj = packageJson.get(PACKAGE_LICENSE);
            if (licenceObj instanceof JsonObject)
            {
                return  ((JsonObject) licenceObj).getString("type", "");
            }
            else
            {
               return packageJson.getString(PACKAGE_LICENSE, "");
            }
        }
        else if (packageJson.containsKey("licenses"))
        {
            final JsonArray licenses = packageJson.getJsonArray("licenses");
            if (licenses.size() == 1)
            {
               return licenses.getJsonObject(0).getString("type", "");
            }
            else if (licenses.size() > 1)
            {
                String license = "(";
                for (JsonValue licenseObj : licenses)
                {
                    if (licenseObj instanceof JsonObject)
                    {
                        String licensePart = licenseObj.asJsonObject().getString("type", "");
                        if (!licensePart.trim().isEmpty())
                        {
                            license += license.length() > 1 ? (" OR " + licensePart) : licensePart;
                        }
                    }
                }
                return license.length() == 1 ? "" : (license + ")");
            }
        }
        return "";
    }
}
