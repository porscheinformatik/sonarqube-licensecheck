package at.porscheinformatik.sonarqube.licensecheck.projectlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_SET;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_ALLOWED;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_PROJECT_KEY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        return Arrays.stream(configuration.getStringArray(PROJECT_LICENSE_SET))
            .map(idx -> {
                String idxProp = "." + idx + ".";
                String projectKey = configuration.get(PROJECT_LICENSE_SET + idxProp + FIELD_PROJECT_KEY).orElse(null);
                String license = configuration.get(PROJECT_LICENSE_SET + idxProp + FIELD_LICENSE).orElse(null);
                Boolean allowed =
                    configuration.getBoolean(PROJECT_LICENSE_SET + idxProp + FIELD_ALLOWED).orElse(Boolean.FALSE);
                return new ProjectLicense(projectKey, license, allowed);
            }).collect(Collectors.toList());
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
