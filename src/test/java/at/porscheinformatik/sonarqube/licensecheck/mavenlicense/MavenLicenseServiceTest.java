package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.Configuration;

import java.util.Optional;

public class MavenLicenseServiceTest
{

    @Test
    public void getLicenseMap()
    {
        String regex = "Regex";
        String licenseNameRegex = "LicenseNameRegex";
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.get(LICENSE_REGEX)).thenReturn(Optional.of("[{\"regex\": \"" + regex + "\", \"licenseNameRegEx\": \"" + licenseNameRegex + "\"}]"));

        MavenLicenseService mavenLicenseService = new MavenLicenseService(configuration);
        assertEquals(1, mavenLicenseService.getLicenseMap().size());
        assertTrue(mavenLicenseService.getLicenseMap().containsKey(regex));
        assertTrue(mavenLicenseService.getLicenseMap().containsValue(licenseNameRegex));
    }
}
