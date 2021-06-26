package at.porscheinformatik.sonarqube.licensecheck.licensemapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;
import static at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMapping.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMapping.FIELD_REGEX;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.server.platform.PersistentSettings;

import at.porscheinformatik.sonarqube.licensecheck.utils.IOUtils;

/**
 * @deprecated for reading use {@link LicenseMappingService}
 */
@ServerSide
@Deprecated
public class LicenseMappingSettingsService
{
    private static final Logger LOGGER = Loggers.get(LicenseMappingSettingsService.class);

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final Configuration configuration;
    private final LicenseMappingService licenseMappingService;

    public LicenseMappingSettingsService(PersistentSettings persistentSettings, Configuration configuration,
        LicenseMappingService licenseMappingService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.configuration = configuration;
        this.licenseMappingService = licenseMappingService;
    }

    public void init()
    {
        if (configuration.get(LICENSE_MAPPING).orElse(null) != null)
        {
            LOGGER.debug("Maven license mappings exists.");
        }
        else if (configuration.get(LICENSE_REGEX).orElse(null) != null)
        {
            LOGGER.info("Migrating old settings to new format for Maven license mappings");
            migrateOldSettings();
        }
        else
        {
            LOGGER.info("No old config found, import default_license_mapping");

            try (InputStream in = LicenseMappingSettingsService.class.getResourceAsStream(
                "default_license_mapping.json"))
            {
                String licenseMappingListString = IOUtils.readToString(in);
                saveSettings(LicenseMapping.fromString(licenseMappingListString));
            }
            catch (Exception e)
            {
                LOGGER.error("Could not load default_license_mapping.json", e);
            }
        }
    }

    private void saveSettings(List<LicenseMapping> licensMappings)
    {
        String indexes = IntStream.range(1, licensMappings.size() + 1)
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(","));
        persistentSettings.saveProperty(LICENSE_MAPPING, indexes);
        for (int i = 0; i < licensMappings.size(); i++)
        {
            LicenseMapping licenseMapping = licensMappings.get(i);
            String idxProp = "." + (i + 1) + ".";
            persistentSettings.saveProperty(LICENSE_MAPPING + idxProp + FIELD_LICENSE, licenseMapping.getLicense());
            persistentSettings.saveProperty(LICENSE_MAPPING + idxProp + FIELD_REGEX,
                licenseMapping.getRegex().toString());
        }
    }

    private void migrateOldSettings()
    {
        saveSettings(licenseMappingService.getLicenseMappingListOld());
        persistentSettings.saveProperty(LICENSE_REGEX, null);
    }
}
