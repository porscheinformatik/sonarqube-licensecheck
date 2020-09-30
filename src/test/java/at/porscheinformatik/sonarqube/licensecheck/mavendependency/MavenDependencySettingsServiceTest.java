package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.Configuration;
import org.sonar.server.platform.PersistentSettings;

public class MavenDependencySettingsServiceTest
{
    @Test
    public void addMavenDependency()
    {
        MavenDependencySettingsService mavenDependencySettingsService = new MavenDependencySettingsService(
            Mockito.mock(Configuration.class), Mockito.mock(PersistentSettings.class), Mockito.mock(MavenDependencyService.class));

        String key = "StringTestDependency";
        String license = "StringTestLicense";
        mavenDependencySettingsService.addMavenDependency(key, license);
        MavenDependency mavenDependency = new MavenDependency(key, license);

        Assert.assertTrue(mavenDependencySettingsService.hasDependency(mavenDependency));

        mavenDependency = new MavenDependency("TestDependency", "TestLicense");
        mavenDependencySettingsService.addMavenDependency(mavenDependency);

        Assert.assertTrue(mavenDependencySettingsService.hasDependency(mavenDependency));
    }

    @Test
    public void deleteMavenDependency()
    {
        MavenDependencySettingsService mavenDependencySettingsService = new MavenDependencySettingsService(
            Mockito.mock(Configuration.class), Mockito.mock(PersistentSettings.class), Mockito.mock(MavenDependencyService.class));

        String key = "TestDependency";
        MavenDependency mavenDependency = new MavenDependency(key, "TestLicense");
        mavenDependencySettingsService.addMavenDependency(mavenDependency);

        Assert.assertTrue(mavenDependencySettingsService.hasDependency(mavenDependency));

        mavenDependencySettingsService.deleteMavenDependency(key);

        Assert.assertFalse(mavenDependencySettingsService.hasDependency(mavenDependency));
    }
}
