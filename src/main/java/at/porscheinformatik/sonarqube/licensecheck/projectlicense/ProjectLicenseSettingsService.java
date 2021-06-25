package at.porscheinformatik.sonarqube.licensecheck.projectlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.PROJECT_LICENSE_SET;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_ALLOWED;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense.FIELD_PROJECT_KEY;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.server.platform.PersistentSettings;

/**
 * @deprecated for reading use {@link ProjectLicenseService}
 */
@ServerSide
@Deprecated
public class ProjectLicenseSettingsService
{
    private static final Logger LOGGER = Loggers.get(ProjectLicenseSettingsService.class);

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final Configuration configuration;
    private final ProjectLicenseService projectLicenseService;

    public ProjectLicenseSettingsService(PersistentSettings persistentSettings, Configuration configuration,
        ProjectLicenseService projectLicenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.configuration = configuration;
        this.projectLicenseService = projectLicenseService;
    }

    public void init()
    {
        if (configuration.get(PROJECT_LICENSE_SET).orElse(null) == null &&
            configuration.get(PROJECT_LICENSE_KEY).orElse(null) != null)
        {
            LOGGER.info("Migrating old settings to new format for project licenses");

            migrateOldSettings();
        }
    }

    private void migrateOldSettings()
    {
        saveSettings(projectLicenseService.getProjectLicenseListOld());
        persistentSettings.saveProperty(PROJECT_LICENSE_KEY, null);
    }

    private void saveSettings(List<ProjectLicense> projectLicenses)
    {
        Collections.sort(projectLicenses);

        String indexes = IntStream.range(1, projectLicenses.size() + 1)
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(","));
        persistentSettings.saveProperty(PROJECT_LICENSE_SET, indexes);
        for (int i = 0; i < projectLicenses.size(); i++)
        {
            ProjectLicense projectLicense = projectLicenses.get(i);
            String idxProp = "." + i + ".";
            persistentSettings.saveProperty(PROJECT_LICENSE_SET + idxProp + FIELD_PROJECT_KEY,
                projectLicense.getProjectKey());
            persistentSettings.saveProperty(PROJECT_LICENSE_SET + idxProp + FIELD_LICENSE, projectLicense.getLicense());
            persistentSettings.saveProperty(PROJECT_LICENSE_SET + idxProp + FIELD_ALLOWED,
                projectLicense.getAllowed().toString());
        }
    }
}
