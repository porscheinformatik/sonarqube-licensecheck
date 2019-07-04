package at.porscheinformatik.sonarqube.licensecheck.maven;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;

public class MavenDependencyScannerTest
{
    @Test
    public void testLicensesAreFound()
    {
        File moduleDir = new File(".");

        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        when(dependencyService.getMavenDependencies()).thenReturn(Arrays.asList(new MavenDependency("org.apache.*", "Apache-2.0")));
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService);

        // -
        Set<Dependency> dependencies = scanner.scan(moduleDir);

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
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService);

        File moduleDir = Files.createTempDirectory("lala").toFile();
        moduleDir.deleteOnExit();
        Set<Dependency> dependencies = scanner.scan(moduleDir);

        assertThat(dependencies.size(), is(0));
    }
}
