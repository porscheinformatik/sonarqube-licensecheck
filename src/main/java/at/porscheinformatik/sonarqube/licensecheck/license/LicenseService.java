package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;

import java.util.Collection;
import java.util.List;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.server.ServerSide;

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseService;

@ServerSide
@ScannerSide
public class LicenseService
{
    private final Configuration configuration;
    private final ProjectLicenseService projectLicenseService;

    public LicenseService(Configuration configuration, ProjectLicenseService projectLicenseService)
    {
        super();
        this.configuration = configuration;
        this.projectLicenseService = projectLicenseService;
    }

    public List<License> getLicenses(InputProject module)
    {
        List<License> globalLicenses = getLicenses();

        if (module == null)
        {
            return globalLicenses;
        }

        Collection<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList(module.key());

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
        String licenseString = configuration.get(LICENSE_KEY).orElse(null);
        return License.fromString(licenseString);
    }
}
