package at.porscheinformatik.sonarqube.licensecheck.maven;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import java.io.File;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DirectoryFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryFinder.class);

    private DirectoryFinder() {}

    public static File getPomPath(Dependency findPathOfDependency, File mavenRepositoryDir) {
        String[] ids = findPathOfDependency.getName().split(":");
        String groupId = ids[0];
        String artifactId = ids[1];

        String tmp =
            groupId.replace(".", "/") +
            "/" +
            artifactId +
            "/" +
            findPathOfDependency.getVersion() +
            "/" +
            artifactId +
            "-" +
            findPathOfDependency.getVersion() +
            ".pom";

        return new File(mavenRepositoryDir, tmp);
    }

    public static File getMavenRepsitoryDir(String userSettings, String globalSettings) {
        if (System.getProperty("maven.repo.local") != null) {
            return new File(System.getProperty("maven.repo.local"));
        }
        String mavenOpts = System.getenv("MAVEN_OPTS");
        if (mavenOpts != null) {
            try {
                String[] opts = CommandLineUtils.translateCommandline(mavenOpts);
                for (String opt : opts) {
                    if (opt.startsWith("-Dmaven.repo.local=")) {
                        String repoPath = opt.substring("-Dmaven.repo.local=".length());
                        return new File(repoPath);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Could not parse MAVEN_OPTS: " + mavenOpts, e);
                // ignore
            }
        }

        File mavenConfFile = new File(System.getProperty("user.home"), ".m2/settings.xml");
        if (userSettings != null) {
            mavenConfFile = new File(userSettings);
        }
        if (mavenConfFile.exists() && mavenConfFile.isFile()) {
            File localRepositoryPath = SettingsXmlParser
                .parseXmlFile(mavenConfFile)
                .getLocalRepositoryPath();
            if (localRepositoryPath != null) {
                return localRepositoryPath;
            }
        }

        mavenConfFile = new File(System.getenv("MAVEN_HOME"), "conf/settings.xml");
        if (globalSettings != null) {
            mavenConfFile = new File(globalSettings);
        }
        if (mavenConfFile.exists() && mavenConfFile.isFile()) {
            File localRepositoryPath = SettingsXmlParser
                .parseXmlFile(mavenConfFile)
                .getLocalRepositoryPath();
            if (localRepositoryPath != null) {
                return localRepositoryPath;
            }
        }

        return new File(System.getProperty("user.home"), ".m2/repository");
    }
}
