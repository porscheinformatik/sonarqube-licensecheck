package at.porscheinformatik.sonarqube.licensecheck.maven;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;

public class MavenDependencyScannerTest
{
    @Test
    public void testLicensesAreFound()
    {
        File moduleDir = new File(".");

        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile("Indiana University.*"), "UNI");
        licenseMap.put(Pattern.compile(".*BSD.*"), "BSD-3-Clause");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        Mockito.when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService);

        // -
        List<Dependency> dependencies = scanner.scan(moduleDir);

        assertThat(dependencies.size(), Matchers.greaterThan(0));

        // -
        for (Dependency dep : dependencies)
        {
            if ("org.codehaus.staxmate:staxmate".equals(dep.getName()))
            {
                assertThat(dep.getLicense(), is("BSD-3-Clause"));
            }
            else if ("xpp3:xpp3".equals(dep.getName()) && "1.1.4c".equals(dep.getVersion()))
            {
                assertThat(dep.getLicense(), is("UNI"));
            }
        }
    }

    @Test
    public void testNullMavenProjectDependencies() throws IOException
    {
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService);

        File moduleDir = Files.createTempDirectory("lala").toFile();
        moduleDir.deleteOnExit();
        List<Dependency> dependencies = scanner.scan(moduleDir);

        assertThat(dependencies.size(), is(0));
    }
}
