package at.porscheinformatik.sonarqube.licensecheck.dependencymapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.DEPENDENCY_MAPPING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.server.platform.PersistentSettings;

public class DependencyMappingSettingsServiceTest
{
    @Test
    public void migrate()
    {
        PersistentSettings persistentSettings = mock(PersistentSettings.class);

        Configuration configuration = mock(Configuration.class);
        when(configuration.get(any())).thenReturn(Optional.empty());
        when(configuration.get(ALLOWED_DEPENDENCIES_KEY)).thenReturn(Optional.of(
            "[{ \"nameMatches\": \"test\", \"license\": \"TEST\" }, "
                + "{\"nameMatches\":\"test2\", \"license\":\"TEST\"}]"));

        DependencyMappingSettingsService service =
            new DependencyMappingSettingsService(persistentSettings, configuration,
                new DependencyMappingService(configuration));

        service.init();

        verify(persistentSettings).saveProperty(DEPENDENCY_MAPPING, "1,2");
        verify(persistentSettings).saveProperty(DEPENDENCY_MAPPING + ".1." + DependencyMapping.FIELD_KEY, "test");
        verify(persistentSettings).saveProperty(ALLOWED_DEPENDENCIES_KEY, null);

    }
}
