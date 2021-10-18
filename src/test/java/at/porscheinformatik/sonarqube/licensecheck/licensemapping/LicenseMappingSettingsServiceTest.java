package at.porscheinformatik.sonarqube.licensecheck.licensemapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.server.platform.PersistentSettings;

public class LicenseMappingSettingsServiceTest
{
    Configuration configuration;
    PersistentSettings persistentSettings;
    LicenseMappingSettingsService licenseMappingSettingsService;

    @Before
    public void setup()
    {
        configuration = mock(Configuration.class);
        persistentSettings = mock(PersistentSettings.class);
        licenseMappingSettingsService =
            new LicenseMappingSettingsService(persistentSettings, configuration,
                new LicenseMappingService(configuration));

        when(configuration.get(anyString())).thenReturn(Optional.empty());
    }

    @Test
    public void init()
    {
        licenseMappingSettingsService.init();

        verify(persistentSettings, atLeastOnce()).saveProperty(anyString(), anyString());
    }

    @Test
    public void migrate()
    {
        when(configuration.get(LICENSE_REGEX)).thenReturn(
            Optional.of("[{ \"regex\": \"test\", \"license\": \"TEST\" }]"));

        licenseMappingSettingsService.init();

        verify(persistentSettings).saveProperty(LICENSE_MAPPING, "1");
        verify(persistentSettings).saveProperty(LICENSE_REGEX, null);
    }
}
