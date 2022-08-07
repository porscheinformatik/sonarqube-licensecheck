package at.porscheinformatik.sonarqube.licensecheck.nugetlicense;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
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

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;

public class NugetLicenseDependencyScannerTest
{
    private static final File RESOURCE_FOLDER = new File("src/test/resources");

    private SensorContext createContext(File folder)
    {
        SensorContext context = mock(SensorContext.class);
        InputFile packageJson = mock(InputFile.class);
        when(packageJson.language()).thenReturn("json");
        when(packageJson.filename()).thenReturn("licenses.json");
        when(packageJson.relativePath()).thenReturn("/licenses.json");
        when(packageJson.type()).thenReturn(InputFile.Type.MAIN);
        try
        {
            when(packageJson.inputStream()).thenAnswer(i -> new FileInputStream(new File(folder, "licenses.json")));
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

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
            new Dependency("MonoGame.Content.Builder.Task", "3.8.0.1641", "MS-PL"),
            new Dependency("MonoGame.Framework.DesktopGL", "3.8.0.1641", "MS-PL"),
            new Dependency("CommandLineParser", "2.8.0", "License.md"),
            new Dependency("Newtonsoft.Json", "13.0.1", "MIT")));
    }

    @Test
    public void testNoPackageJson()
    {
        Set<Dependency> dependencies = createScanner().scan(createContext(new File("src")));

        assertThat(dependencies, hasSize(0));
    }

    private Scanner createScanner()
    {
        LicenseMappingService licenseMappingService = mock(LicenseMappingService.class);
        when(licenseMappingService.mapLicense(anyString())).thenCallRealMethod();

        return new NugetLicenseDependencyScanner(licenseMappingService);
    }
}
