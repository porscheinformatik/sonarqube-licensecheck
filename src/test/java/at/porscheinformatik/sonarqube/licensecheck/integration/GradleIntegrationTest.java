package at.porscheinformatik.sonarqube.licensecheck.integration;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class GradleIntegrationTest {

    private static File projectRoot;

    @Before
    public void setup() throws IOException, InterruptedException {
        projectRoot = new File("target/testProject");
        FileUtils.deleteDirectory(projectRoot);
        projectRoot.mkdirs();

        File buildGradleSrc = new File(this.getClass().getClassLoader().getResource("gradle/build.gradle").getFile());
        File buildGradleTrg = new File(projectRoot, "build.gradle");
        if (!buildGradleTrg.exists()) {
            FileUtils.copyFile(buildGradleSrc, buildGradleTrg);
        }
    }


    @Parameterized.Parameters
    public static List<String> data() {
        return Arrays.asList("5.1.1", "4.10.3", "3.5.1");
    }

    @Parameterized.Parameter
    public String version;

    @Test
    public void scanWithMatch() throws IOException {
        GradleWrapperResolver.loadGradleWrapper(projectRoot, version);

        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile(".*Apache.*2.*"), "Apache-2.0");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        when(dependencyService.getMavenDependencies()).thenReturn(Arrays.asList(new MavenDependency("org.apache.*", "Apache-2.0")));

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
