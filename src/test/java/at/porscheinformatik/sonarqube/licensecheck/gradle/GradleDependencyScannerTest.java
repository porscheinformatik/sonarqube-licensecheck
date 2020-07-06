package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.mockito.Mockito.when;

public class GradleDependencyScannerTest extends TestCase {

    private MavenLicenseService mockLicenseService() {
        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        return licenseService;
    }

    @Test
    public void testScannerWithMissingJsonFile() {
        GradleDependencyScanner scanner = new GradleDependencyScanner(mockLicenseService());
        Set<Dependency> dependencies = scanner.scan(new File("/abc"));
        assertEquals(0, dependencies.size());
    }

    @Test
    public void testScanner() {
        GradleDependencyScanner scanner = new GradleDependencyScanner(mockLicenseService());
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        Set<Dependency> dependencies = scanner.scan(new File(absolutePath));
        assertEquals(43, dependencies.size());
        dependencies.forEach(System.out::println);
    }

    @Test
    public void testRegEx() {
        String s = ".*Apache.*2.*";
        Pattern p = Pattern.compile(s);
        assertTrue(p.matcher("Apache License, Version 2.0").matches());
    }
}
