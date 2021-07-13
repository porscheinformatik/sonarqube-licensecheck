package at.porscheinformatik.sonarqube.licensecheck.projectlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_SET;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_ALLOWED;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_PROJECT_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.sonar.api.config.Configuration;

public class ProjectLicenseTest
{
    @Test
    public void getAllProjectLicenses()
    {
        List<ProjectLicense> projectLicenseList = createProjectLicenseService().getProjectLicenseList();

        assertThat(projectLicenseList.size(), is(2));
    }

    @Test
    public void getSpecificProjectLicenses()
    {
        Collection<ProjectLicense> projectLicenseList = createProjectLicenseService().getProjectLicenseList("proj1");

        assertThat(projectLicenseList.size(), is(1));
        assertThat(projectLicenseList.iterator().next().getLicense(), is("MIT"));
    }

    private ProjectLicenseService createProjectLicenseService()
    {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getStringArray(PROJECT_LICENSE_SET)).thenReturn(new String[]{"1", "2"});
        when(configuration.get(PROJECT_LICENSE_SET + ".1." + FIELD_PROJECT_KEY)).thenReturn(Optional.of("proj1"));
        when(configuration.get(PROJECT_LICENSE_SET + ".2." + FIELD_PROJECT_KEY)).thenReturn(Optional.of("proj2"));
        when(configuration.get(PROJECT_LICENSE_SET + ".1." + FIELD_LICENSE)).thenReturn(Optional.of("MIT"));
        when(configuration.get(PROJECT_LICENSE_SET + ".2." + FIELD_LICENSE)).thenReturn(Optional.of("BSD"));
        when(configuration.getBoolean(PROJECT_LICENSE_SET + ".1." + FIELD_ALLOWED)).thenReturn(Optional.of(false));
        when(configuration.getBoolean(PROJECT_LICENSE_SET + ".2." + FIELD_ALLOWED)).thenReturn(Optional.of(true));
        return new ProjectLicenseService(configuration);
    }
}
