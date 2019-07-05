package at.porscheinformatik.sonarqube.licensecheck.npm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;

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
            LOGGER.info("No package.json file found in {} - skipping NPM dependency scan", moduleDir.getPath());
            return Collections.emptySet();
        }

        LOGGER.info("Scanning for NPM dependencies");

        File nodeModulesFolder = new File(packageJsonFile.getParentFile(), "node_modules");
        if (!nodeModulesFolder.exists() || !nodeModulesFolder.isDirectory())
        {
            return Collections.emptySet();
        }

        try (InputStream fis = new FileInputStream(packageJsonFile); JsonReader jsonReader = Json.createReader(fis))
        {
            Deque<String> transitiveDependencies = new ArrayDeque<>();
            return getDependenciesFrom(jsonReader.readObject(), nodeModulesFolder, transitiveDependencies);
        }
        catch (IOException e)
        {
            LOGGER.error("Error reading package.json", e);
            return Collections.emptySet();
        }
    }

    private Set<Dependency> getDependenciesFrom(JsonObject packageJsonObject, File nodeModulesFolder,
        Collection<String> transitiveDependencies)
    {
        JsonObject jsonObjectDependencies = packageJsonObject.getJsonObject("dependencies");
        if (jsonObjectDependencies != null)
        {
            return dependencyParser(jsonObjectDependencies, nodeModulesFolder, transitiveDependencies);
        }
        return Collections.emptySet();
    }

    private Set<Dependency> dependencyParser(JsonObject jsonDependencies, File nodeModulesFolder,
        Collection<String> transitiveDependencies)
    {
        Set<Dependency> dependencies = new LinkedHashSet<>();

        for (String packageName : jsonDependencies.keySet())
        {
            moduleCheck(nodeModulesFolder, packageName, dependencies, transitiveDependencies);
        }

        return dependencies;
    }

    private void moduleCheck(File nodeModulesFolder, String packageName, Set<Dependency> dependencies,
        Collection<String> transitiveDependencies)
    {
        if (transitiveDependencies.contains(packageName)) {
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
