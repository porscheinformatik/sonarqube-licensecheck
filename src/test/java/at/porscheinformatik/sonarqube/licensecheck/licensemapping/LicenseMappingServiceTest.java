package at.porscheinformatik.sonarqube.licensecheck.licensemapping;

import org.junit.Test;
import org.sonar.api.config.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMapping.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMapping.FIELD_REGEX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LicenseMappingServiceTest {
    @Test
    public void getLicenseMap() {
        LicenseMappingService service = createService();

        Map<Pattern, String> licenseMap = service.getLicenseMap();

        assertThat(licenseMap.size(), is(2));
    }

    @Test
    public void mapLicense() {
        LicenseMappingService service = createService();

        String license = service.mapLicense("Apache Software License, 2.0");

        assertThat(license, is("ASL2"));
    }

    private LicenseMappingService createService() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getStringArray(LICENSE_MAPPING)).thenReturn(new String[]{"1", "2"});
        when(configuration.get(LICENSE_MAPPING + ".1." + FIELD_REGEX)).thenReturn(Optional.of("MIT"));
        when(configuration.get(LICENSE_MAPPING + ".1." + FIELD_LICENSE)).thenReturn(Optional.of("MIT"));
        when(configuration.get(LICENSE_MAPPING + ".2." + FIELD_REGEX)).thenReturn(Optional.of("^Apache.*2.*$"));
        when(configuration.get(LICENSE_MAPPING + ".2." + FIELD_LICENSE)).thenReturn(Optional.of("ASL2"));
        return new LicenseMappingService(configuration);
    }
}
