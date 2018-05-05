package at.porscheinformatik.sonarqube.licensecheck.gradle.license;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.apache.maven.model.License;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static at.porscheinformatik.sonarqube.licensecheck.gradle.TestDataBuilder.license;
import static at.porscheinformatik.sonarqube.licensecheck.gradle.TestDataBuilder.licenseMap;
import static org.mockito.Mockito.when;

public class LicenseMatcherTest {

    @Test
    public void handleNullMavenLicenseService() {
        LicenseMatcher licenseMatcher = new LicenseMatcher(null);

        Assert.assertNull(licenseMatcher.findLicenseFromLicenseMap("some license"));
    }

    @Test
    public void matchReturnsLicense() {
        MavenLicenseService mavenLicenseService = Mockito.mock(MavenLicenseService.class);
        when(mavenLicenseService.getLicenseMap()).thenReturn(
            licenseMap(".*[Aa]pache.*1.*", "Apache-1.0"));

        LicenseMatcher underTest = new LicenseMatcher(mavenLicenseService);

        License license = license("Some Apache Software License in version 1");

        Assert.assertEquals(underTest.findLicenseFromLicenseMap(license.getName()), "Apache-1.0");
        Assert.assertEquals("Apache-1.0", underTest.viaLicenseMap(license.getName()));
    }

    @Test
    public void noMatchReturnsInputString() {
        MavenLicenseService mavenLicenseService = Mockito.mock(MavenLicenseService.class);
        when(mavenLicenseService.getLicenseMap()).thenReturn(
            licenseMap(".*[Aa]pache.*1.*", "Apache-1.0"));

        LicenseMatcher underTest = new LicenseMatcher(mavenLicenseService);

        License license = license("This is my personal license");

        Assert.assertEquals(underTest.findLicenseFromLicenseMap(license.getName()), null);
        Assert.assertEquals("This is my personal license", underTest.viaLicenseMap(license.getName()));
    }
}
