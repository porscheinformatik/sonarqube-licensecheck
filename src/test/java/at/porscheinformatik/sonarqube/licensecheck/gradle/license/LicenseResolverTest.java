package at.porscheinformatik.sonarqube.licensecheck.gradle.license;

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static at.porscheinformatik.sonarqube.licensecheck.gradle.TestDataBuilder.mavenDependencies;
import static org.mockito.Mockito.when;

public class LicenseResolverTest {

    @Test
    public void handleNullMavenDependencyService() {
        LicenseResolver licenseResolver = new LicenseResolver(null);

        Assert.assertEquals("", licenseResolver.byPackage("com.sample:my-artifact"));
    }

    @Test
    public void matchViaDependency() {
        MavenDependencyService mavenDependencyService = Mockito.mock(MavenDependencyService.class);
        when(mavenDependencyService.getMavenDependencies()).thenReturn(
            mavenDependencies("com\\.sample.*", "sample-license"));
        LicenseResolver licenseResolver = new LicenseResolver(mavenDependencyService);

        Assert.assertEquals("sample-license", licenseResolver.byPackage("com.sample:my-artifact"));
    }
}
