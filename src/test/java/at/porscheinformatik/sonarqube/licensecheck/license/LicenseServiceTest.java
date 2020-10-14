package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.fs.InputProject;

import java.util.ArrayList;
import java.util.Optional;

public class LicenseServiceTest {

    @Test
    public void getLicenses() {
        Configuration configuration = Mockito.mock(Configuration.class);
        LicenseService licenseService = new LicenseService(configuration, Mockito.mock(ProjectLicenseService.class));

        Mockito.when(configuration.get(LICENSE_KEY)).thenReturn(Optional.of(""));
        assertEquals(0, licenseService.getLicenses().size());

        String name = "Name";
        String identifier = "Identifier";
        String status = "false";
        Mockito.when(configuration.get(LICENSE_KEY)).thenReturn(
            Optional.of("[{\"name\": \"" + name + "\", \"identifier\": \"" + identifier + "\", \"status\":\"" + status + "\"}]".replaceAll(",",
                LicenseService.COMMA_PLACEHOLDER)));

        assertEquals(1, licenseService.getLicenses().size());
        assertTrue(licenseService.getLicenses().contains(new License(name, identifier, status)));
    }

    @Test
    public void testGetLicenses() {
        Configuration configuration = Mockito.mock(Configuration.class);
        InputProject inputProject = Mockito.mock(InputProject.class);
        ProjectLicenseService projectLicenseService = Mockito.mock(ProjectLicenseService.class);
        LicenseService licenseService = new LicenseService(configuration, projectLicenseService);

        Mockito.when(configuration.get(LICENSE_KEY)).thenReturn(Optional.of(""));
        assertEquals(0, licenseService.getLicenses(null).size());

        String key = "Key";
        Mockito.when(inputProject.key()).thenReturn(key);

        Mockito.when(configuration.get(PROJECT_LICENSE_KEY)).thenReturn(Optional.of(""));
        assertEquals(0, licenseService.getLicenses(inputProject).size());

        Mockito.when(projectLicenseService.getProjectLicenseList(key)).thenReturn(new ArrayList<>());
        assertEquals(0, licenseService.getLicenses(inputProject).size());

        String statusFalse = "false";
        String name = "Name";
        String identifier = "Identifier";
        Mockito.when(configuration.get(LICENSE_KEY)).thenReturn(
            Optional.of("[{\"name\": \"" + name + "\", \"identifier\": \"" + identifier + "\", \"status\":\"" + statusFalse + "\"}]".replaceAll(",",
                LicenseService.COMMA_PLACEHOLDER)));

        assertEquals(1, licenseService.getLicenses(inputProject).size());

        String statusTrue = "true";
        Mockito.when(projectLicenseService.getProjectLicenseList(key)).thenReturn(new ArrayList<>(){{
            add(new ProjectLicense(key, identifier, statusTrue));
        }});
        assertEquals(licenseService.getLicenses(inputProject).get(0).getStatus(), statusTrue);
    }
}
