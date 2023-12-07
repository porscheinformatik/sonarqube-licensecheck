package at.porscheinformatik.sonarqube.licensecheck.gradle;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;

public class GradleDependencyScannerTest {

    private SensorContext createContext(File folder) {
        SensorContext context = mock(SensorContext.class);
        FileSystem fileSystem = mock(FileSystem.class);
        Configuration config = mock(Configuration.class);
        when(fileSystem.baseDir()).thenReturn(folder);
        when(fileSystem.workDir()).thenReturn(folder);
        when(config.get(anyString())).thenReturn(Optional.empty());
        when(context.fileSystem()).thenReturn(fileSystem);
        when(context.config()).thenReturn(config);
        return context;
    }

    @Test
    public void testScannerWithMissingJsonFile() {
        GradleDependencyScanner scanner = new GradleDependencyScanner(mockLicenseService());
        Set<Dependency> dependencies = scanner.scan(createContext(new File("/abc")));
        assertEquals(0, dependencies.size());
    }

    @Test
    public void testScanner() {
        GradleDependencyScanner scanner = new GradleDependencyScanner(mockLicenseService());
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        Set<Dependency> dependencies = scanner.scan(createContext(new File(absolutePath)));
        assertEquals(43, dependencies.size());
    }

    @Test
    public void testScannerWithConfiguredDirectory() {
        GradleDependencyScanner scanner = new GradleDependencyScanner(mockLicenseService());
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        SensorContext context = createContext(new File(absolutePath));
        when(context.config().get(LicenseCheckPropertyKeys.GRADLE_JSON_REPORT_PATH))
            .thenReturn(Optional.of("build/my-reports/license-details.json"));
        Set<Dependency> dependencies = scanner.scan(context);
        assertEquals(43, dependencies.size());
    }

    private LicenseMappingService mockLicenseService() {
        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        LicenseMappingService licenseService = Mockito.mock(LicenseMappingService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        return licenseService;
    }
}
