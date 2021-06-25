package at.porscheinformatik.sonarqube.licensecheck.gradle;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;
import org.mockito.Mockito;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;

public class GradleDependencyScannerTest
{
    @Test
    public void testScannerWithMissingJsonFile()
    {
        GradleDependencyScanner scanner = new GradleDependencyScanner(mockLicenseService());
        Set<Dependency> dependencies = scanner.scan(new File("/abc"));
        assertEquals(0, dependencies.size());
    }

    @Test
    public void testScanner()
    {
        GradleDependencyScanner scanner = new GradleDependencyScanner(mockLicenseService());
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        Set<Dependency> dependencies = scanner.scan(new File(absolutePath));
        assertEquals(43, dependencies.size());
    }

    private LicenseMappingService mockLicenseService()
    {
        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        LicenseMappingService licenseService = Mockito.mock(LicenseMappingService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        return licenseService;
    }
}
