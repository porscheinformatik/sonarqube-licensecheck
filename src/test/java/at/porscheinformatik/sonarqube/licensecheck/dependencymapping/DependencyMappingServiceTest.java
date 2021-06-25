package at.porscheinformatik.sonarqube.licensecheck.dependencymapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.DEPENDENCY_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_OVERWRITE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.sonar.api.config.Configuration;

public class DependencyMappingServiceTest
{
    @Test
    public void loadConfiguration()
    {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getStringArray(DEPENDENCY_MAPPING)).thenReturn(new String[]{"1", "2"});
        when(configuration.get(any())).thenReturn(Optional.empty());
        when(configuration.getBoolean(any())).thenReturn(Optional.empty());
        when(configuration.get(DEPENDENCY_MAPPING + ".1." + FIELD_KEY)).thenReturn(Optional.of("kee"));
        when(configuration.get(DEPENDENCY_MAPPING + ".1." + FIELD_LICENSE)).thenReturn(Optional.of("MIT"));
        when(configuration.getBoolean(DEPENDENCY_MAPPING + ".1." + FIELD_OVERWRITE)).thenReturn(Optional.of(true));

        DependencyMappingService dependencyMappingService = new DependencyMappingService(configuration);

        List<DependencyMapping> dependencyMappings = dependencyMappingService.getDependencyMappings();
        assertThat(dependencyMappings.size(), is(2));
        assertThat(dependencyMappings.get(0), is(new DependencyMapping("kee", "MIT", true)));
    }
}
