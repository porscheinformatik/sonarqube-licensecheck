package at.porscheinformatik.sonarqube.licensecheck.projectLicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

@ServerSide
@ScannerSide
public class ProjectLicenseService
{
    private final Configuration configuration;

    public ProjectLicenseService(Configuration configuration)
    {
        super();
        this.configuration = configuration;
    }

    public List<ProjectLicense> getProjectLicenseList()
    {
        String projectLicenseString = configuration.get(PROJECT_LICENSE_KEY).orElse(null);

        if (StringUtils.isNotEmpty(projectLicenseString))
        {
            return ProjectLicense.fromString(projectLicenseString);
        }

        return new ArrayList<>();
    }

    public Collection<ProjectLicense> getProjectLicenseList(String projectKey)
    {
        Collection<ProjectLicense> allProjectLicenses = getProjectLicenseList();
        Collection<ProjectLicense> projectSpecificLicenses = new ArrayList<>();

        for (ProjectLicense projectLicense : allProjectLicenses)
        {
            if (projectLicense.getProjectKey().equals(projectKey))
            {
                projectSpecificLicenses.add(projectLicense);
            }
        }

        return projectSpecificLicenses;
    }
}
