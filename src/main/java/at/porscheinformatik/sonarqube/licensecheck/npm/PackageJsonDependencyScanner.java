package at.porscheinformatik.sonarqube.licensecheck.npm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;

public class PackageJsonDependencyScanner implements Scanner
{
    @Override
    public List<Dependency> scan(File file, String mavenProjectDependencies)
    {
        File packageJsonFile = new File(file, "package.json");
        final List<Dependency> dependencies = new ArrayList<>();

        if (packageJsonFile.exists())
        {
            InputStream fis;
            try
            {
                fis = new FileInputStream(packageJsonFile);
                JsonReader jsonReader = Json.createReader(fis);
                JsonObject jsonObject = jsonReader.readObject();
                JsonObject jsonObjectdependencies = jsonObject.getJsonObject("dependencies");
                String jsonObjectString = jsonObjectdependencies.toString();

                dependencyParser(dependencies, jsonObjectString, packageJsonFile);
            }
            catch (FileNotFoundException e1)
            {
                throw new RuntimeException(e1);
            }
            return dependencies;
        }
        return Collections.emptyList();
    }

    private void dependencyParser(List<Dependency> dependencies, String jsonObjectString, File packageJsonFile)
    {
        JsonParser parser = Json.createParser(new StringReader(jsonObjectString));
        File nodeModulesFolder = new File(packageJsonFile.getParentFile(), "node_modules");

        while (parser.hasNext())
        {
            JsonParser.Event event = parser.next();
            switch (event)
            {
                case KEY_NAME:
                    if (nodeModulesFolder.exists() && nodeModulesFolder.isDirectory())
                    {
                        moduleCheck(nodeModulesFolder, parser.getString(), dependencies);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void moduleCheck(File nodeModulesFolder, String identifier, List<Dependency> dependencies)
    {
        File module = new File(nodeModulesFolder, identifier);
        checkPackageJsonForDependencies(module, dependencies, identifier);
    }

    private static void checkPackageJsonForDependencies(File module, List<Dependency> dependencies, String name)
    {
        if (module.exists() && module.isDirectory())
        {
            File packageFile = new File(module, "package.json");

            try
            {
                InputStream fis = new FileInputStream(packageFile);
                JsonReader jsonReader = Json.createReader(fis);
                JsonObject jsonObject = jsonReader.readObject();
                dependencies
                    .add(new Dependency(name, jsonObject.getString("version"), jsonObject.getString("license")));
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
