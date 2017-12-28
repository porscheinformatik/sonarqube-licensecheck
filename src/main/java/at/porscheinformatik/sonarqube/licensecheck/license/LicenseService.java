package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;

import java.util.Collection;
import java.util.List;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseService;

@ServerSide
@ScannerSide
public class LicenseService
{
    private final Settings settings;
    private final ProjectLicenseService projectLicenseService;

    public LicenseService(Settings settings, ProjectLicenseService projectLicenseService)
    {
        super();
        this.settings = settings;
        this.projectLicenseService = projectLicenseService;
    }

    public List<License> getLicenses(ProjectDefinition module)
    {
        List<License> globalLicenses = getLicenses();

        if (module == null)
        {
            return globalLicenses;
        }

        Collection<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList(module.getKey());

        for (License license : globalLicenses)
        {
            for (ProjectLicense projectLicense : projectLicenses)
            {
                if (license.getIdentifier().equals(projectLicense.getLicense()))
                {
                    license.setStatus(projectLicense.getStatus()); //override the stati of the globalLicenses
                }
            }
        }

        return globalLicenses;
    }

    public List<License> getLicenses()
    {
        String licenseString = settings.getString(LICENSE_KEY);
        return License.fromString(licenseString);
    }
}
