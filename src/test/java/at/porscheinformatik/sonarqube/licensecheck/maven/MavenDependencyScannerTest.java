package at.porscheinformatik.sonarqube.licensecheck.maven;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
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

        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        when(dependencyService.getMavenDependencies()).thenReturn(
            singletonList(new MavenDependency("org.apache.*", "Apache-2.0")));
        Scanner scanner = new MavenDependencyScanner(mockLicenseService(), dependencyService);

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

    private MavenLicenseService mockLicenseService()
    {
        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        return licenseService;
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

    @Test
    public void mavenDependencyMappingHandledBeforePomLicense()
    {
        File moduleDir = new File(".");
        MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        List<MavenDependency> mavenDependencies =
            singletonList(new MavenDependency("org.apache.commons:commons.*", "TEST"));
        when(dependencyService.getMavenDependencies()).thenReturn(mavenDependencies);
        Scanner scanner = new MavenDependencyScanner(mockLicenseService(), dependencyService);

        Set<Dependency> dependencies = scanner.scan(moduleDir);

        Dependency commonsLang = dependencies.stream()
            .filter(d -> "org.apache.commons:commons-lang3".equals(d.getName()))
            .findFirst().orElse(null);

        assertThat(commonsLang.getLicense(), is("TEST"));
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
}
