package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_SET;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.MAVEN_LICENSE_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicense.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicense.FIELD_REGEX;

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
 * @deprecated for reading use {@link MavenLicenseService}
 */
@ServerSide
@Deprecated
public class MavenLicenseSettingsService
{
    private static final Logger LOGGER = Loggers.get(MavenLicenseSettingsService.class);

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final Configuration configuration;
    private final MavenLicenseService mavenLicenseService;

    public MavenLicenseSettingsService(PersistentSettings persistentSettings, Configuration configuration,
        MavenLicenseService mavenLicenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.configuration = configuration;
        this.mavenLicenseService = mavenLicenseService;
    }

    public void init()
    {
        if (configuration.get(MAVEN_LICENSE_MAPPING).orElse(null) != null)
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

            try (InputStream in = MavenLicenseSettingsService.class.getResourceAsStream("default_license_mapping.json"))
            {
                String mavenLicenseListString = IOUtils.readToString(in);
                saveSettings(MavenLicense.fromString(mavenLicenseListString));
            }
            catch (Exception e)
            {
                LOGGER.error("Could not load default_license_mapping.json", e);
            }
        }
    }

    private void saveSettings(List<MavenLicense> mavenLicenses)
    {
        String indexes = IntStream.range(1, mavenLicenses.size())
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(","));
        persistentSettings.saveProperty(MAVEN_LICENSE_MAPPING, indexes);
        for (int i = 0; i < mavenLicenses.size(); i++)
        {
            MavenLicense mavenLicense = mavenLicenses.get(i);
            String idxProp = "." + i + ".";
            persistentSettings.saveProperty(MAVEN_LICENSE_MAPPING + idxProp + FIELD_LICENSE, mavenLicense.getLicense());
            persistentSettings.saveProperty(MAVEN_LICENSE_MAPPING + idxProp + FIELD_REGEX,
                mavenLicense.getRegex().toString());
        }
    }

    private void migrateOldSettings()
    {
        saveSettings(mavenLicenseService.getMavenLicenseListOld());
        persistentSettings.saveProperty(LICENSE_REGEX, null);
    }
}
