package at.porscheinformatik.sonarqube.licensecheck.npm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;

import org.sonar.api.config.Settings;

public class PackageJsonDependencyScanner implements Scanner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageJsonDependencyScanner.class);

    private final Settings settings;

    public PackageJsonDependencyScanner(Settings settings)
    {
        this.settings = settings;
    }

    @Override
    public Set<Dependency> scan(File moduleDir)
    {
        File packageJsonFile = new File(moduleDir, "package.json");

        if (!packageJsonFile.exists())
        {
            return Collections.emptySet();
        }

        File nodeModulesFolder = new File(packageJsonFile.getParentFile(), "node_modules");
        if (!nodeModulesFolder.exists() || !nodeModulesFolder.isDirectory())
        {
            return Collections.emptySet();
        }

        try (InputStream fis = new FileInputStream(packageJsonFile); JsonReader jsonReader = Json.createReader(fis))
        {
            return getDependenciesFrom(jsonReader.readObject(), nodeModulesFolder);
        }
        catch (IOException e)
        {
            LOGGER.error("Error reading package.json", e);
            return Collections.emptySet();
        }
    }

    private Set<Dependency> getDependenciesFrom(JsonObject packageJsonObject, File nodeModulesFolder)
    {
        JsonObject jsonObjectDependencies = packageJsonObject.getJsonObject("dependencies");
        if (jsonObjectDependencies != null)
        {
            return dependencyParser(jsonObjectDependencies, nodeModulesFolder);
        }
        return Collections.emptySet();
    }

    private Set<Dependency> dependencyParser(JsonObject jsonDependencies, File nodeModulesFolder)
    {
        Set<Dependency> dependencies = new LinkedHashSet<>();

        for (String packageName : jsonDependencies.keySet())
        {
            moduleCheck(nodeModulesFolder, packageName, dependencies);
        }

        return dependencies;
    }

    private void moduleCheck(File nodeModulesFolder, String packageName, Set<Dependency> dependencies)
    {
        if (dependencies.stream().anyMatch(d -> packageName.equals(d.getName())))
        {
            LOGGER.warn("Circular dependency detected in {}. Current stack of transitive deps: {}", packageName, dependencies);
            return;
        }

        File moduleFolder = new File(nodeModulesFolder, packageName);

        if (moduleFolder.exists() && moduleFolder.isDirectory())
        {
            File packageFile = new File(moduleFolder, "package.json");

            try (InputStream fis = new FileInputStream(packageFile); JsonReader jsonReader = Json.createReader(fis))
            {
                JsonObject packageJsonObject = jsonReader.readObject();
                if (packageJsonObject == null)
                {
                    return;
                }

                String license = "";
                if (packageJsonObject.containsKey("license"))
                {
                    license = packageJsonObject.getString("license");
                }
                else if (packageJsonObject.containsKey("licenses"))
                {
                    JsonArray licenses = packageJsonObject.getJsonArray("licenses");
                    if (licenses.size() > 0)
                    {
                        license = licenses.getJsonObject(0).getString("type");
                    }
                }

                dependencies.add(new Dependency(packageName, packageJsonObject.getString("version"), license));

                if (settings.getBoolean(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITVE_DEPS))
                {
                    dependencies.addAll(getDependenciesFrom(packageJsonObject, nodeModulesFolder));
                }
            }
            catch (FileNotFoundException e)
            {
                LOGGER.error("Could not find package.json", e);
            }
            catch (Exception e)
            {
                LOGGER.error("Error adding dependency " + packageName, e);
            }
        }
    }
}
