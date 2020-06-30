package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import junit.framework.TestCase;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class GradleDependencyScannerTest extends TestCase {

    @Test
    public void testScannerWithMissingJsonFile() {
        MapSettings settings = new MapSettings();
        Configuration configuration = settings.asConfig();
        MavenDependencyService mavenDependencyServiceT = new MavenDependencyService(configuration);
        GradleDependencyScanner scanner = new GradleDependencyScanner(mavenDependencyServiceT);
        Set<Dependency> dependencies =
            scanner.scan(new File("/abc"));
        assertEquals(0, dependencies.size());
    }

    @Test
    public void testScanner() {
        MapSettings settings = new MapSettings();
        Configuration configuration = settings.asConfig();
        MavenDependencyService mavenDependencyServiceT = new MavenDependencyService(configuration);
        GradleDependencyScanner scanner = new GradleDependencyScanner(mavenDependencyServiceT);

        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        Set<Dependency> dependencies =
            scanner.scan(new File(absolutePath));
        assertEquals(38, dependencies.size());
    }
}
