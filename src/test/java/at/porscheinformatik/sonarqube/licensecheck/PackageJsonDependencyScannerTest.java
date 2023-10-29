package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

public class PackageJsonDependencyScannerTest
{
    private static final Path RESOURCE_FOLDER = Path.of("src/test/resources");

    private static final Logger LOGGER = Loggers.get(PackageJsonDependencyScannerTest.class);

    private SensorContext createContext(Path moduleBaseDir)
    {
        SensorContext context = mock(SensorContext.class);
        DefaultFileSystem fileSystem = new DefaultFileSystem(moduleBaseDir);
        try {
            // Provide all **source** files in the moduleBaseDir
            Files.walk(moduleBaseDir).forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        return;
                    }
                    if (path.toString().contains("/node_modules/")) {
                        LOGGER.info("Ignoring file {}, because it is in a node_modules folder", path);
                        return;
                    }
                    var lines = Files.readAllLines(path);
                    InputFile inputFile =  TestInputFileBuilder.create("test", moduleBaseDir.toFile(), path.toFile())
                        // Required to get correct metadata
                        .setContents(lines.stream().collect(Collectors.joining("\n")))
                        // Otherwise .inputStream() will throw a NPE
                        .setCharset(Charset.forName("UTF-8"))
                        .build();
                    LOGGER.info("Added {} to the fs", path);
                    fileSystem.add(inputFile);
    
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
    
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(context.fileSystem()).thenReturn(fileSystem);
        return context;
    }

    @Test
    public void testHappyPath()
    {
        Set<Dependency> dependencies = createScanner().scan(createContext(RESOURCE_FOLDER.resolve("example")));

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0")));
    }

    @Test
    public void testTransitive()
    {
        Set<Dependency> dependencies = createScanner(true).scan(createContext(RESOURCE_FOLDER.resolve("example")));

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0"),
            new Dependency("linkedlist", "1.0.1", "LGPLv3"),
            new Dependency("retry", "0.10.1", "MIT")));
    }

    @Test
    public void testPackageJsonNotInBaseDir() throws Exception {
        Set<Dependency> dependencies = createScanner().scan(createContext(RESOURCE_FOLDER.resolve("example_nested")));
         assertThat(dependencies, containsInAnyOrder(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0")));

    }

    @Test
    public void testNoPackageJson()
    {
        Set<Dependency> dependencies = createScanner().scan(createContext(RESOURCE_FOLDER.resolve("no_package_json")));

        assertThat(dependencies, hasSize(0));
    }

    @Test
    public void testNoNodeModules()
    {
        Set<Dependency> dependencies = createScanner().scan(createContext(RESOURCE_FOLDER.resolve("example/node_modules/arangojs")));

        assertThat(dependencies, hasSize(0));
    }

    @Test
    public void testLicenseInDeprecatedLicenseFormat()
    {
        final Set<Dependency> dependencies = createScanner().scan(createContext(RESOURCE_FOLDER.resolve("deprecated_project")));

        assertEquals(1, dependencies.size());

        final Dependency expectedDependency = new Dependency("dynamic-dedupe", "0.3.0", "MIT");
        assertEquals(expectedDependency, dependencies.toArray()[0]);
    }

    @Test
    public void testLicenseInDeprecatedLicensesFormat()
    {
        final Set<Dependency> dependencies = createScanner().scan(createContext(RESOURCE_FOLDER.resolve("deprecated_multilicense_project")));

        assertEquals(1, dependencies.size());

        final Dependency expectedDependency = new Dependency("some-module", "1.7.1", "(MIT OR LGPLv3)");
        assertEquals(expectedDependency, dependencies.toArray()[0]);
    }

    private Scanner createScanner()
    {
        return createScanner(false);
    }

    private Scanner createScanner(boolean resolveTransitiveDeps)
    {
        LicenseMappingService licenseMappingService = mock(LicenseMappingService.class);
        when(licenseMappingService.mapLicense(anyString())).thenCallRealMethod();
        return new PackageJsonDependencyScanner(licenseMappingService, resolveTransitiveDeps);
    }
}
