package at.porscheinformatik.sonarqube.licensecheck.projectLicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

@ServerSide
public class ProjectLicenseSettingsService
{
    /** This is not official API */
    private final PersistentSettings persistentSettings;
    private final Settings settings;
    private final ProjectLicenseService projectLicenseService;

    public ProjectLicenseSettingsService(PersistentSettings persistentSettings, Settings settings,
        ProjectLicenseService projectLicenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.settings = settings;
        this.projectLicenseService = projectLicenseService;
    }

    public boolean addProjectLicense(String license, String projectName, String status, String projectKey)
    {
        ProjectLicense newProjectLicense = new ProjectLicense(license, projectName, status, projectKey);

        if (!checkIfListContains(newProjectLicense))
        {
            addProjectLicense(newProjectLicense);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean addProjectLicense(ProjectLicense newProjectLicense)
    {
        List<ProjectLicense> projectLicense = projectLicenseService.getProjectLicenseList();

        if (!projectLicense.contains(newProjectLicense))
        {
            projectLicense.add(newProjectLicense);
            saveSettings(projectLicense);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean checkIfListContains(ProjectLicense projectLicense)
    {
        List<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList();
        return projectLicenses.contains(projectLicense);
    }

    public void deleteProjectLicense(String license, String project)
    {
        List<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList();
        Iterator<ProjectLicense> i = projectLicenses.iterator();

        ProjectLicense tmpProjectLicense = null;
        while (i.hasNext())
        {
            tmpProjectLicense = i.next();
            String licenseFromList = tmpProjectLicense.getLicense();
            String projectFromList = tmpProjectLicense.getProjectName();
            if (licenseFromList.equals(license) && projectFromList.equals(project))
            {
                i.remove();
            }
        }
        saveSettings(projectLicenses);
    }

    private void saveSettings(List<ProjectLicense> projectLicenses)
    {
        String projectLicenseString = "";

        for (ProjectLicense project : projectLicenses)
        {
            projectLicenseString += project.getLicense()
                + "~"
                + project.getProjectName()
                + "~"
                + project.getStatus()
                + "~"
                + project.getProjectKey()
                + ";";
        }

        settings.setProperty(PROJECT_LICENSE_KEY, projectLicenseString);
        persistentSettings.saveProperty(PROJECT_LICENSE_KEY, projectLicenseString);
    }

    public void updateProjectLicense(ProjectLicense oldProjectLicense, ProjectLicense newProjectLicense)
    {
        deleteProjectLicense(oldProjectLicense.getLicense(), oldProjectLicense.getProjectName());
        addProjectLicense(newProjectLicense);
        sortProjectLicenses();
    }

    public void sortProjectLicenses()
    {
        List<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList();
        Collections.sort(projectLicenses);
        saveSettings(projectLicenses);
    }
}
