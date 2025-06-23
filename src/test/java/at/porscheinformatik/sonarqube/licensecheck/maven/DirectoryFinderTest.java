package at.porscheinformatik.sonarqube.licensecheck.maven;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.junit.Test;

public class DirectoryFinderTest {

    @Test
    public void checkMavenRepoLocal() {
        String mavenRepoLocalOld = System.getProperty("maven.repo.local");
        System.setProperty("maven.repo.local", "/tmp/maven-repo");
        File repoDir = DirectoryFinder.getMavenRepsitoryDir(null, null);
        if (mavenRepoLocalOld != null) {
            System.setProperty("maven.repo.local", mavenRepoLocalOld);
        } else {
            System.clearProperty("maven.repo.local");
        }
        assertEquals(repoDir.getAbsolutePath(), "/tmp/maven-repo");
    }
}
