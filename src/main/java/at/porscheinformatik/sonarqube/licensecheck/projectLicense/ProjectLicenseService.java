package at.porscheinformatik.sonarqube.licensecheck.projectLicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;

@ServerSide
@BatchSide
public class ProjectLicenseService
{
    private final Settings settings;

    public ProjectLicenseService(Settings settings)
    {
        super();
        this.settings = settings;
    }

    public List<ProjectLicense> getProjectLicenseList()
    {
        List<ProjectLicense> projectLicenseList = new ArrayList<>();

        String projectLicenseString = settings.getString(PROJECT_LICENSE_KEY);
        if (StringUtils.isNotEmpty(projectLicenseString))
        {
            String[] projectLicenseEntries = projectLicenseString.split(";");
            for (String projectLicenseEntry : projectLicenseEntries)
            {
                String[] projectEntryParts = projectLicenseEntry.split("~");
                projectLicenseList.add(new ProjectLicense(projectEntryParts[0], projectEntryParts[1],
                    projectEntryParts[2], projectEntryParts[3]));
            }
        }

        return projectLicenseList;
    }

    public List<ProjectLicense> getProjectLicenseList(String projectKey)
    {
        List<ProjectLicense> allProjectLicenses = getProjectLicenseList();
        List<ProjectLicense> projectSpecificLicenses = new ArrayList<>();

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
