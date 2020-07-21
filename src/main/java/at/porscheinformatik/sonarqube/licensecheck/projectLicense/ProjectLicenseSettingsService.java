package at.porscheinformatik.sonarqube.licensecheck.projectLicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

@ServerSide
public class ProjectLicenseSettingsService
{
    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;
    private final ProjectLicenseService projectLicenseService;

    public ProjectLicenseSettingsService(PersistentSettings persistentSettings,
        ProjectLicenseService projectLicenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.projectLicenseService = projectLicenseService;
    }

    public boolean addProjectLicense(ProjectLicense newProjectLicense)
    {
        List<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList();

        if (!projectLicenses.contains(newProjectLicense))
        {
            projectLicenses.add(newProjectLicense);
            saveSettings(projectLicenses);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void deleteProjectLicense(String projectKey, String license)
    {
        List<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList();
        Iterator<ProjectLicense> i = projectLicenses.iterator();

        while (i.hasNext())
        {
            ProjectLicense tmpProjectLicense = i.next();
            String licenseFromList = tmpProjectLicense.getLicense();
            String projectFromList = tmpProjectLicense.getProjectKey();
            if (licenseFromList.equals(license) && projectFromList.equals(projectKey))
            {
                i.remove();
            }
        }
        saveSettings(projectLicenses);
    }

    private void saveSettings(List<ProjectLicense> projectLicenses)
    {
        Collections.sort(projectLicenses);
        String projectLicensesString = ProjectLicense.createString(projectLicenses);
        persistentSettings.saveProperty(PROJECT_LICENSE_KEY, projectLicensesString);
    }

    public boolean updateProjectLicense(ProjectLicense newProjectLicense)
    {
        deleteProjectLicense(newProjectLicense.getProjectKey(), newProjectLicense.getLicense());
        return addProjectLicense(newProjectLicense);
    }
}
