package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;;

class DirectoryFinder
{

    private DirectoryFinder()
    {
    }

    public static File getPomPath(Dependency findPathOfDependency, File mavenRepositoryDir)
    {

        String[] IDs = findPathOfDependency.getName().split(":");
        String groupId = IDs[0];
        String artifactId = IDs[1];

        String tmp = groupId.replace(".", "/")
            + "/"
            + artifactId
            + "/"
            + findPathOfDependency.getVersion()
            + "/"
            + artifactId
            + "-"
            + findPathOfDependency.getVersion()
            + ".pom";

        return new File(mavenRepositoryDir, tmp);
    }

    public static File getMavenRepsitoryDir()
    {
        File mavenConfFile = new File(System.getProperty("user.home"), ".m2/settings.xml");
        if (!mavenConfFile.exists())
        {
            mavenConfFile = new File(System.getenv("MAVEN_HOME"), "conf/settings.xml");
        }

        return mavenConfFile.exists() ? SettingsXmlParser.parseXmlFile(mavenConfFile).getLocalRepositoryPath() : null;
    }
}
