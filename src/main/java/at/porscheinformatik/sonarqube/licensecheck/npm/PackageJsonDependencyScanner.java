package at.porscheinformatik.sonarqube.licensecheck.npm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;

public class PackageJsonDependencyScanner implements Scanner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageJsonDependencyScanner.class);

    @Override
    public List<Dependency> scan(File file)
    {
        File packageJsonFile = new File(file, "package.json");

        if (packageJsonFile.exists())
        {
            File nodeModulesFolder = new File(packageJsonFile.getParentFile(), "node_modules");
            if (!nodeModulesFolder.exists() || !nodeModulesFolder.isDirectory())
            {
                return Collections.emptyList();
            }

            try (InputStream fis = new FileInputStream(packageJsonFile);
                JsonReader jsonReader = Json.createReader(fis))
            {
                return getDependenciesFrom(jsonReader.readObject(), nodeModulesFolder);
            }
            catch (IOException e)
            {
                LOGGER.error("Error reading package.json", e);
            }
        }
        return Collections.emptyList();
    }

    private static List<Dependency> getDependenciesFrom(JsonObject packageJsonObject, File nodeModulesFolder)
    {
        JsonObject jsonObjectDependencies = packageJsonObject.getJsonObject("dependencies");
        if (jsonObjectDependencies != null)
        {
            return dependencyParser(jsonObjectDependencies, nodeModulesFolder);
        }
        return Collections.emptyList();
    }

    private static List<Dependency> dependencyParser(JsonObject jsonDependencies, File nodeModulesFolder)
    {
        List<Dependency> dependencies = new ArrayList<>();

        for (String packageName : jsonDependencies.keySet())
        {
            moduleCheck(nodeModulesFolder, packageName, dependencies);
        }

        return dependencies;
    }

    private static void moduleCheck(File nodeModulesFolder, String packageName, List<Dependency> dependencies)
    {
        File moduleFolder = new File(nodeModulesFolder, packageName);

        if (moduleFolder.exists() && moduleFolder.isDirectory())
        {
            File packageFile = new File(moduleFolder, "package.json");

            try (InputStream fis = new FileInputStream(packageFile); JsonReader jsonReader = Json.createReader(fis))
            {
                JsonObject jsonObject = jsonReader.readObject();
                if (jsonObject != null)
                {
                    String license = null;
                    if (jsonObject.containsKey("license"))
                    {
                        license = jsonObject.getString("license");
                    }
                    else if (jsonObject.containsKey("licenses"))
                    {
                        JsonArray licenses = jsonObject.getJsonArray("licenses");
                        if (licenses.size() > 0)
                        {
                            license = licenses.getJsonObject(0).getString("type");
                        }
                    }

                    if (license != null)
                    {
                        dependencies.add(new Dependency(packageName, jsonObject.getString("version"), license));
                    }
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
