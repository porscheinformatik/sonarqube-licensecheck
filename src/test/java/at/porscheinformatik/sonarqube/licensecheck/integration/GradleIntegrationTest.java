package at.porscheinformatik.sonarqube.licensecheck.integration;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleProjectResolver;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class GradleIntegrationTest {

    private static File projectRoot;

    @Before
    public void setup() throws IOException {
        projectRoot = GradleProjectResolver.prepareGradleProject();
    }


    @Parameterized.Parameters
    public static List<String> data() {
        return Arrays.asList("5.1.1", "4.10.3", "3.5.1");
    }

    @Parameterized.Parameter
    public String version;

    @Test
    public void scanWithMatch() throws IOException {
        GradleProjectResolver.loadGradleWrapper(projectRoot, version);

        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        when(dependencyService.getMavenDependencies()).thenReturn(Collections.singletonList(new MavenDependency("org.apache.*", "Apache-2.0")));

        GradleDependencyScanner gradleDependencyScanner = new GradleDependencyScanner(licenseService, dependencyService);

        List<Dependency> dependencies = gradleDependencyScanner.scan(projectRoot);

        Assert.assertEquals(13, dependencies.size());
        Assert.assertTrue(dependencies.contains(
            new Dependency("org.spockframework:spock-core",
                "1.1-groovy-2.4",
                "Apache-2.0")));
        Assert.assertTrue(dependencies.contains(
            new Dependency("org.tukaani:xz",
                "1.5",
                "Public Domain")));
    }
}
