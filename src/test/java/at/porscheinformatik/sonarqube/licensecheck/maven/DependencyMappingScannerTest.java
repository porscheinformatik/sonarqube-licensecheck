package at.porscheinformatik.sonarqube.licensecheck.maven;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;

public class DependencyMappingScannerTest
{
    @Test
    public void testLicensesAreFound()
    {
        File moduleDir = new File(".");

        Scanner scanner = new MavenDependencyScanner(mockLicenseService());

        // -
        Set<Dependency> dependencies = scanner.scan(createContext(moduleDir));

        assertThat(dependencies.size(), Matchers.greaterThan(0));

        // -
        for (Dependency dep : dependencies)
        {
            if ("org.apache.commons:commons-lang3".equals(dep.getName()))
            {
                assertThat(dep.getLicense(), is("Apache-2.0"));
            }
            else if ("org.codehaus.plexus:plexus-utils".equals(dep.getName()))
            {
                assertThat(dep.getLicense(), is("Apache-2.0"));
            }
        }
    }

    @Test
    public void testNullMavenProjectDependencies() throws IOException
    {
        LicenseMappingService licenseService = Mockito.mock(LicenseMappingService.class);
        Scanner scanner = new MavenDependencyScanner(licenseService);

        File moduleDir = Files.createTempDirectory("lala").toFile();
        moduleDir.deleteOnExit();
        Set<Dependency> dependencies = scanner.scan(createContext(moduleDir));

        assertThat(dependencies.size(), is(0));
    }

    @Test
    public void testFindDependency()
    {
        String jarFilePath = new File("src/test/resources/test.jar").getAbsolutePath();
        Dependency dependency = MavenDependencyScanner.findDependency(
            "at.porscheinformatik.test:test:jar:1.2.3:compile:" + jarFilePath + " -- module test (auto)");

        assertThat(dependency.getName(), is("at.porscheinformatik.test:test"));
        assertThat(dependency.getVersion(), is("1.2.3"));
        assertThat(dependency.getPomPath(), endsWith("test.pom"));
    }

    @Test
    public void testFindDependencyWithClassifier()
    {
        String jarFilePath = new File("src/test/resources/test-sources.jar").getAbsolutePath();
        Dependency dependency = MavenDependencyScanner.findDependency(
            "at.porscheinformatik.test:test:jar:sources:2.2:compile:" + jarFilePath + " -- module test (auto)");

        assertThat(dependency.getName(), is("at.porscheinformatik.test:test"));
        assertThat(dependency.getVersion(), is("2.2"));
        assertThat(dependency.getPomPath(), endsWith("test.pom"));
    }

    @Test
    public void testSettingsFromMaven()
    { 
        String oldVal = System.getProperty("sun.java.command");
        try
        {
            System.setProperty("sun.java.command", "thisisatest -X -s src/test/resources/settings-does-not-exist.xml -gs src/test/resources/settings-does-not-exist.xml -B");
            Scanner scanner = new MavenDependencyScanner(mockLicenseService());
            
            Set<Dependency> dependencies = scanner.scan(createContext(new File(".")));
            assertThat(dependencies.size(), is(0));
        }
        finally
        {
            System.setProperty("sun.java.command", oldVal);
        }
    }

    private SensorContext createContext(File moduleDir)
    {
        SensorContext context = mock(SensorContext.class);
        InputFile pomXml = mock(InputFile.class);
        when(pomXml.language()).thenReturn("xml");
        when(pomXml.filename()).thenReturn("pom.xml");
        when(pomXml.uri()).thenReturn(new File(moduleDir, "pom.xml").toURI());
        when(pomXml.relativePath()).thenReturn("/pom.xml");
        when(pomXml.type()).thenReturn(InputFile.Type.MAIN);
        try
        {
            when(pomXml.inputStream()).thenAnswer(i -> new FileInputStream(new File(moduleDir, "pom.xml")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        FileSystem fileSystem = new DefaultFileSystem(moduleDir.toPath()).add(pomXml);
        when(context.fileSystem()).thenReturn(fileSystem);

        return context;
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
