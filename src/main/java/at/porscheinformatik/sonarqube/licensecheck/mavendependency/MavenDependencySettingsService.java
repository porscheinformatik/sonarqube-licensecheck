package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.MAVEN_DEPENDENCY_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency.FIELD_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency.FIELD_LICENSE;

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
 * @deprecated for reading use {@link MavenDependencyService}
 */
@ServerSide
@Deprecated
public class MavenDependencySettingsService
{
    private static final Logger LOGGER = Loggers.get(MavenDependencySettingsService.class);

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final Configuration configuration;
    private final MavenDependencyService mavenDependencyService;

    public MavenDependencySettingsService(PersistentSettings persistentSettings, Configuration configuration,
        MavenDependencyService mavenDependencyService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.configuration = configuration;
        this.mavenDependencyService = mavenDependencyService;
    }

    public void init()
    {
        if (configuration.get(MAVEN_DEPENDENCY_MAPPING).orElse(null) == null
            && configuration.get(ALLOWED_DEPENDENCIES_KEY).orElse(null) != null)
        {
            LOGGER.info("Migrating old settings to new format for Maven dependency mappings");
            migrateOldSettings();
        }
    }

    private void saveSettings(List<MavenDependency> mavenDependencies)
    {
        Collections.sort(mavenDependencies);

        String indexes = IntStream.range(1, mavenDependencies.size())
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(","));
        persistentSettings.saveProperty(MAVEN_DEPENDENCY_MAPPING, indexes);
        for (int i = 0; i < mavenDependencies.size(); i++)
        {
            MavenDependency mavenDep = mavenDependencies.get(i);
            String idxProp = "." + i + ".";
            persistentSettings.saveProperty(MAVEN_DEPENDENCY_MAPPING + idxProp + FIELD_KEY, mavenDep.getKey());
            persistentSettings.saveProperty(MAVEN_DEPENDENCY_MAPPING + idxProp + FIELD_LICENSE,
                mavenDep.getLicense());
        }
    }

    private void migrateOldSettings()
    {
        saveSettings(mavenDependencyService.getMavenDependenciesOld());
        persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, null);
    }
}
