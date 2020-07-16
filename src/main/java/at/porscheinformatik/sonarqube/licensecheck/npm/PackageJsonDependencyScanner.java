package at.porscheinformatik.sonarqube.licensecheck.npm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;

public class PackageJsonDependencyScanner implements Scanner
{
    private static final Logger LOGGER = Loggers.get(PackageJsonDependencyScanner.class);

    private boolean resolveTransitiveDeps;

    public PackageJsonDependencyScanner(boolean resolveTransitiveDeps)
    {
        this.resolveTransitiveDeps = resolveTransitiveDeps;
    }

    @Override
    public Set<Dependency> scan(File moduleDir)
    {
        File packageJsonFile = new File(moduleDir, "package.json");

        if (!packageJsonFile.exists())
        {
            LOGGER.info("No package.json file found in {} - skipping NPM dependency scan", moduleDir.getPath());
            return Collections.emptySet();
        }

        LOGGER.info("Scanning for NPM dependencies");

        return dependencyParser(moduleDir, packageJsonFile);
    }

    private Set<Dependency> dependencyParser(File baseDir, File packageJsonFile)
    {
        Set<Dependency> dependencies = new HashSet<>();

        try (InputStream fis = new FileInputStream(packageJsonFile);
            JsonReader jsonReader = Json.createReader(fis))
        {
            JsonObject packageJson = jsonReader.readObject();

            JsonObject packageJsonDependencies = packageJson.getJsonObject("dependencies");
            if (packageJsonDependencies != null)
            {
                scanDependencies(baseDir, packageJsonDependencies.keySet(), dependencies);
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Error reading package.json", e);
        }

        return dependencies;
    }

    private void scanDependencies(File baseDir, Set<String> packageNames, Set<Dependency> dependencies)
    {
        LOGGER.info("Scanning NPM packages " + packageNames);

        for (String packageName : packageNames)
        {
            if (dependencies.stream().anyMatch(d -> packageName.equals(d.getName())))
            {
                LOGGER.debug("Package {} has already been encountered and will not be scanned again", packageName);
                continue;
            }

            File packageJsonFile = new File(baseDir, "node_modules/" + packageName + "/package.json");
            if (!packageJsonFile.exists())
            {
                LOGGER.warn("No package.json file found for package {} in node_modules - skipping dependency",
                        packageName);
                continue;
            }

            try (InputStream fis = new FileInputStream(packageJsonFile);
                JsonReader jsonReader = Json.createReader(fis))
            {
                JsonObject packageJson = jsonReader.readObject();
                if (packageJson != null)
                {
                    String license = "";
                    if (packageJson.containsKey("license"))
                    {
                        license = packageJson.getString("license", null);
                    }
                    else if (packageJson.containsKey("licenses"))
                    {
                        JsonArray licenses = packageJson.getJsonArray("licenses");
                        if (licenses.size() > 0)
                        {
                            license = licenses.getJsonObject(0).getString("type", null);
                        }
                    }

                    dependencies.add(new Dependency(packageName, packageJson.getString("version", null), license));

                    if (resolveTransitiveDeps)
                    {
                        JsonObject packageJsonDependencies = packageJson.getJsonObject("dependencies");
                        if (packageJsonDependencies != null)
                        {
                            scanDependencies(baseDir, packageJsonDependencies.keySet(), dependencies);
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
}
