package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sonar.api.config.Configuration;
import org.sonar.server.platform.PersistentSettings;

public class MavenLicenseSettingsServiceTest
{

    @Test
    public void addMavenLicense()
    {
        MavenLicenseSettingsService mavenLicenseSettingsService = new MavenLicenseSettingsService(
            Mockito.mock(PersistentSettings.class), Mockito.mock(Configuration.class), Mockito.mock(MavenLicenseService.class));

        String regex = "StringLicenseRegex";
        String key = "StringLicenseKey";
        mavenLicenseSettingsService.addMavenLicense(regex, key);

        Assert.assertTrue(mavenLicenseSettingsService.checkIfListContains(new MavenLicense(regex, key)));

        MavenLicense mavenLicense = new MavenLicense("LicenseRegex", "LicenseKey");
        mavenLicenseSettingsService.addMavenLicense(mavenLicense);
        Assert.assertTrue(mavenLicenseSettingsService.checkIfListContains(mavenLicense));
    }

    @Test
    public void deleteMavenLicense()
    {
        MavenLicenseSettingsService mavenLicenseSettingsService = new MavenLicenseSettingsService(
            Mockito.mock(PersistentSettings.class), Mockito.mock(Configuration.class), Mockito.mock(MavenLicenseService.class));

        String regex = "LicenseRegex";
        MavenLicense mavenLicense = new MavenLicense(regex, "LicenseKey");
        mavenLicenseSettingsService.addMavenLicense(mavenLicense);
        Assert.assertTrue(mavenLicenseSettingsService.checkIfListContains(mavenLicense));

        mavenLicenseSettingsService.deleteMavenLicense(regex);

        Assert.assertFalse(mavenLicenseSettingsService.checkIfListContains(mavenLicense));
    }
}
