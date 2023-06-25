package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_SET;
import static at.porscheinformatik.sonarqube.licensecheck.license.License.FIELD_ALLOWED;
import static at.porscheinformatik.sonarqube.licensecheck.license.License.FIELD_ID;
import static at.porscheinformatik.sonarqube.licensecheck.license.License.FIELD_NAME;

import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.server.ServerSide;

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
                    license.setAllowed(projectLicense.getAllowed()); //override the stati of the globalLicenses
                }
            }
        }

        return globalLicenses;
    }

    public List<License> getLicenses()
    {
        return Arrays.stream(configuration.getStringArray(LICENSE_SET))
            .map(idx -> {
                String idxProp = "." + idx + ".";
                String name = configuration.get(LICENSE_SET + idxProp + FIELD_NAME).orElse(null);
                String identifier = configuration.get(LICENSE_SET + idxProp + FIELD_ID).orElse(null);
                Boolean allowed = configuration.getBoolean(LICENSE_SET + idxProp + FIELD_ALLOWED)
                    .orElse(Boolean.FALSE);
                return new License(name, identifier, allowed.toString());
            }).collect(Collectors.toList());
    }

}
