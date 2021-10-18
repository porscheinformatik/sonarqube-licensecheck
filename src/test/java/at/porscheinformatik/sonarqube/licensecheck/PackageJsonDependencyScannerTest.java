package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;

import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

public class PackageJsonDependencyScannerTest
{
    private static final File RESOURCE_FOLDER = new File("src/test/resources");

    private SensorContext createContext(File folder)
    {
        SensorContext context = mock(SensorContext.class);
        InputFile packageJson = mock(InputFile.class);
        when(packageJson.language()).thenReturn("json");
        when(packageJson.filename()).thenReturn("package.json");
        when(packageJson.relativePath()).thenReturn("/package.json");
        when(packageJson.type()).thenReturn(InputFile.Type.MAIN);
        try
        {
            when(packageJson.inputStream()).thenAnswer(i -> new FileInputStream(new File(folder, "package.json")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        FileSystem fileSystem = new DefaultFileSystem(folder.toPath()).add(packageJson);
        when(context.fileSystem()).thenReturn(fileSystem);
        return context;
    }

    @Test
    public void testHappyPath()
    {
        Set<Dependency> dependencies = createScanner().scan(createContext(RESOURCE_FOLDER));

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0")));
    }

    @Test
    public void testTransitive()
    {
        Set<Dependency> dependencies = createScanner(true).scan(createContext(RESOURCE_FOLDER));

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0"),
            new Dependency("linkedlist", "1.0.1", "LGPLv3"),
            new Dependency("retry", "0.10.1", "MIT")));
    }

    @Test
    public void testNoPackageJson()
    {
        Set<Dependency> dependencies = createScanner().scan(createContext(new File("src")));

        assertThat(dependencies, hasSize(0));
    }

    @Test
    public void testNoNodeModules()
    {
        Set<Dependency> dependencies = createScanner().scan(createContext(new File(RESOURCE_FOLDER, "node_modules/arangojs")));

        assertThat(dependencies, hasSize(0));
    }

    @Test
    public void testLicenseInDeprecatedLicenseFormat()
    {
        final Set<Dependency> dependencies = createScanner().scan(createContext(new File(RESOURCE_FOLDER, "deprecated_project")));

        assertEquals(1, dependencies.size());

        final Dependency expectedDependency = new Dependency("dynamic-dedupe", "0.3.0", "MIT");
        assertEquals(expectedDependency, dependencies.toArray()[0]);
    }

    @Test
    public void testLicenseInDeprecatedLicensesFormat()
    {
        final Set<Dependency> dependencies = createScanner().scan(createContext(new File(RESOURCE_FOLDER, "deprecated_multilicense_project")));

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
