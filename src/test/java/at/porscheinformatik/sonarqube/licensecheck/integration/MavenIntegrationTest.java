package at.porscheinformatik.sonarqube.licensecheck.integration;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleProjectResolver;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.mockito.Mockito.when;

public class MavenIntegrationTest {

    private static File projectRoot;

    @Before
    public void setup() throws IOException {
        projectRoot = GradleProjectResolver.prepareGradleProject();

        File buildGradleSrc = new File(this.getClass().getClassLoader().getResource("maven/pom.xml").getFile());
        File buildGradleTrg = new File(projectRoot, "pom.xml");
        if (!buildGradleTrg.exists()) {
            FileUtils.copyFile(buildGradleSrc, buildGradleTrg);
        }
    }

    @Test
    public void scan() {
        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        when(dependencyService.getMavenDependencies()).thenReturn(Arrays.asList(new MavenDependency("org.apache.*", "Apache-2.0")));
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService);

        List<Dependency> dependencies = scanner.scan(projectRoot);

        Assert.assertEquals(5, dependencies.size());
        Assert.assertTrue(dependencies.contains(
            new Dependency("org.spockframework:spock-core",
                "1.1-groovy-2.4",
                "Apache-2.0")));
    }
}
