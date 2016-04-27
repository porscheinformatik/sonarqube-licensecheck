package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

public class MavenDependencyScanner implements Scanner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyScanner.class);

    private final LicenseService licenseService;

    public MavenDependencyScanner(LicenseService licenseService)
    {
        this.licenseService = licenseService;
    }

    @Override
    public List<Dependency> scan(File moduleDir, String mavenProjectDependencies)
    {
        final List<Dependency> mavenDependencies = new ArrayList<>();

        JsonReader jsonReader = Json.createReader(new StringReader(mavenProjectDependencies));
        JsonArray jsonArray = jsonReader.readArray();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            JsonObject checkObject = jsonObject;

            String scope = jsonObject.getString("s");
            if ("compile".equals(scope) || "runtime".equals(scope))
            {
                mavenDependencies.add(new Dependency(jsonObject.getString("k"), jsonObject.getString("v"), " "));
                JsonArray dependencyArray = checkObject.getJsonArray("d");
                checkDependencyForScope(dependencyArray, mavenDependencies);
            }
            else
            {
                JsonArray dependencyArray = checkObject.getJsonArray("d");
                checkDependecyNoScope(dependencyArray, mavenDependencies);
            }
        }
        extractLicenseInfoIfAvailable(mavenDependencies);

        return mavenDependencies;
    }

    private void extractLicenseInfoIfAvailable(List<Dependency> mavenDependencies)
    {
        List<String> stringList = new ArrayList<>();
        File mavenConfFile = new File(System.getProperty("user.home"), ".m2/settings.xml");
        if (!mavenConfFile.exists())
        {
            mavenConfFile = new File(System.getenv("MAVEN_HOME"), "conf/settings.xml");
        }

        String mavenRepositoryDir = mavenConfFile.exists() ? loadAndExtractLocalRepository(mavenConfFile)
            : System.getProperty("user.home") + ".m2/repository";

        for (Dependency dependency : mavenDependencies)
        {
            if (" ".equals(dependency.getLicense()))
            {
                String[] depParts = dependency.getName().split(":");
                File dependencyFile = new File(mavenRepositoryDir,
                    depParts[0].replace(".", "/") + "/" + depParts[1] + "/" + dependency.getVersion());
                mvnPomParser(dependencyFile, stringList, dependency);
            }
        }

        StringBuilder saveString = new StringBuilder();
        for (String s : stringList)
        {
            saveString.append(s);
        }
    }

    private void mvnPomParser(File file, List<String> stringList, Dependency dependency)
    {
        File[] pomFile = file.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File file, String name)
            {
                return name.toLowerCase().endsWith(".pom");
            }
        });

        try
        {
            Model model = new MavenXpp3Reader().read(new FileInputStream(pomFile[0]));
            if (model.getLicenses().isEmpty() || model.getLicenses() == null)
            {
                LOGGER.info("No License found in POM.xml for: " + pomFile[0]);
            }
            else
            {
                List<org.apache.maven.model.License> license = model.getLicenses();
                if (license.size() == 1)
                {
                    if (license.get(0).getName() == null)
                    {
                        license.get(0).setName("no license");
                    }
                    String regexLicenseString = licenseService.getLicensesRegex();
                    for (String s : regexLicenseString.split(";"))
                    {
                        String[] regexLicenseSubParts = s.split("~");
                        if (license.get(0).getName().matches(regexLicenseSubParts[0]))
                        {
                            dependency.setLicense(regexLicenseSubParts[1]);
                            LOGGER.info("SET LICENSE: " + dependency.getLicense());
                        }
                    }
                }
                else
                {
                    for (int i = 0; i < license.size(); i++)
                    {
                        if (license.get(i).getName() == null)
                        {
                            license.get(i).setName("no license");
                        }
                        String regexLicenseString = licenseService.getLicensesRegex();
                        for (String s : regexLicenseString.split(";"))
                        {
                            String[] regexLicenseSubParts = s.split("~");
                            if (license.get(i).getName().matches(regexLicenseSubParts[0]))
                            {
                                dependency.setLicense(regexLicenseSubParts[1]);
                            }
                        }
                        if (!" ".equals(dependency.getLicense()))
                        {
                            break;
                        }
                    }
                }
            }
        }
        catch (IOException | XmlPullParserException e)
        {
            e.printStackTrace();
        }
    }

    private static String loadAndExtractLocalRepository(File file)
    {
        String element = "";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            Element rootElement = document.getDocumentElement();

            element = getLocalRepository("localRepository", rootElement);
            return element;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static String getLocalRepository(String name, Element rootElement)
    {
        NodeList list = rootElement.getElementsByTagName(name);
        if ((list != null) && (list.getLength() > 0))
        {
            NodeList subList = list.item(0).getChildNodes();
            if ((subList != null) && (subList.getLength() > 0))
            {
                return subList.item(0).getNodeValue();
            }
        }
        return null;
    }

    private static void checkDependencyForScope(JsonArray dependencyArray, List<Dependency> mavenDependencies)
    {
        for (int i = 0; i < dependencyArray.size(); i++)
        {
            JsonObject dependencyObject = dependencyArray.getJsonObject(i);

            String scope = dependencyObject.getString("s");
            if ("compile".equals(scope) || "runtime".equals(scope))
            {
                JsonArray jsonArray = dependencyObject.getJsonArray("d");
                mavenDependencies
                    .add(new Dependency(dependencyObject.getString("k"), dependencyObject.getString("v"), " "));

                checkDependencyForScope(jsonArray, mavenDependencies);
            }
        }
    }

    private static void checkDependecyNoScope(JsonArray dependencyArray, List<Dependency> mavenDependencies)
    {
        for (int j = 0; j < dependencyArray.size(); j++)
        {
            JsonObject dependencyObject = dependencyArray.getJsonObject(j);
            String scope = dependencyObject.getString("s");
            if ("compile".equals(scope) || "runtime".equals(scope))
            {
                JsonArray jsonArray = dependencyObject.getJsonArray("d");
                mavenDependencies
                    .add(new Dependency(dependencyObject.getString("k"), dependencyObject.getString("v"), " "));

                checkDependecyNoScope(jsonArray, mavenDependencies);
            }
        }
    }
}
