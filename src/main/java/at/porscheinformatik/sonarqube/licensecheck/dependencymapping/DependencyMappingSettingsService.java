package at.porscheinformatik.sonarqube.licensecheck.dependencymapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.DEPENDENCY_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_LICENSE;

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
 * @deprecated for reading use {@link DependencyMappingService}
 */
@ServerSide
@Deprecated
public class DependencyMappingSettingsService
{
    private static final Logger LOGGER = Loggers.get(DependencyMappingSettingsService.class);

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final Configuration configuration;
    private final DependencyMappingService dependencyMappingService;

    public DependencyMappingSettingsService(PersistentSettings persistentSettings, Configuration configuration,
        DependencyMappingService dependencyMappingService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.configuration = configuration;
        this.dependencyMappingService = dependencyMappingService;
    }

    public void init()
    {
        if (configuration.get(DEPENDENCY_MAPPING).orElse(null) == null
            && configuration.get(ALLOWED_DEPENDENCIES_KEY).orElse(null) != null)
        {
            LOGGER.info("Migrating old settings to new format for Maven dependency mappings");
            migrateOldSettings();
        }
    }

    private void saveSettings(List<DependencyMapping> dependencyMappings)
    {
        Collections.sort(dependencyMappings);

        String indexes = IntStream.range(1, dependencyMappings.size() + 1)
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(","));
        persistentSettings.saveProperty(DEPENDENCY_MAPPING, indexes);
        for (int i = 0; i < dependencyMappings.size(); i++)
        {
            DependencyMapping depMap = dependencyMappings.get(i);
            String idxProp = "." + (i + 1) + ".";
            persistentSettings.saveProperty(DEPENDENCY_MAPPING + idxProp + FIELD_KEY, depMap.getKey());
            persistentSettings.saveProperty(DEPENDENCY_MAPPING + idxProp + FIELD_LICENSE, depMap.getLicense());
        }
    }

    private void migrateOldSettings()
    {
        saveSettings(dependencyMappingService.getDependencyMappingsOld());
        persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, null);
    }
}
