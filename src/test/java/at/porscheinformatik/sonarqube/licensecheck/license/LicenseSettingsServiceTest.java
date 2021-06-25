package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_SET;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.server.platform.PersistentSettings;

import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;

public class LicenseSettingsServiceTest
{
    @Test
    public void migrate()
    {
        Configuration configuration = mock(Configuration.class);
        PersistentSettings persistentSettings = mock(PersistentSettings.class);
        LicenseSettingsService licenseSettingsService = new LicenseSettingsService(persistentSettings, configuration,
            new LicenseService(configuration, mock(ProjectLicenseService.class)));
        when(configuration.getStringArray(anyString())).thenReturn(new String[0]);
        when(configuration.get(anyString())).thenReturn(Optional.empty());

        when(configuration.get(LICENSE_KEY)).thenReturn(Optional.of(
            "[{\"identifier\":\"MIT\",\"name\":\"MIT License\",\"status\":\"true\"},"
                + "{\"identifier\":\"MY\",\"name\":\"MY License\",\"status\":\"false\"}]"));

        licenseSettingsService.init();

        verify(persistentSettings).saveProperty(LICENSE_SET, "1,2");
        verify(persistentSettings).saveProperty(LICENSE_KEY, null);
    }
}
