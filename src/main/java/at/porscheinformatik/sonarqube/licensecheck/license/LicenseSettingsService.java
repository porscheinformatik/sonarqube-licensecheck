package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_SET;
import static at.porscheinformatik.sonarqube.licensecheck.license.License.FIELD_ALLOWED;
import static at.porscheinformatik.sonarqube.licensecheck.license.License.FIELD_ID;
import static at.porscheinformatik.sonarqube.licensecheck.license.License.FIELD_NAME;

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
 * @deprecated for reading licenses, use {@link LicenseService}
 */
@ServerSide
@Deprecated
public class LicenseSettingsService
{
    private static final Logger LOGGER = Loggers.get(LicenseSettingsService.class);

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final Configuration configuration;
    private final LicenseService licenseService;

    public LicenseSettingsService(PersistentSettings persistentSettings, Configuration configuration,
        LicenseService licenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.configuration = configuration;
        this.licenseService = licenseService;
    }

    public void init()
    {
        if (configuration.getStringArray(LICENSE_SET).length > 0)
        {
            LOGGER.debug("License configuration exists.");
        }
        else if (configuration.get(LICENSE_KEY).orElse(null) != null)
        {
            LOGGER.info("Old config found, migrating");

            migrateOldSettings();
        }
        else
        {
            LOGGER.info("No old config found, import spdx_license_list");

            try (InputStream inputStream = LicenseSettingsService.class.getResourceAsStream("spdx_license_list.json"))
            {
                String spdxLicenseListJson = IOUtils.readToString(inputStream);
                List<License> spdxLicenses = License.fromString(spdxLicenseListJson);
                saveSettings(spdxLicenses);
            }
            catch (Exception e)
            {
                LOGGER.error("Could not load spdx_license_list.json", e);
            }
        }
    }

    private void migrateOldSettings()
    {
        if (licenseService.getLicenses().isEmpty())
        {
            List<License> licensesOld = licenseService.getLicensesOld();
            LOGGER.info("Migrating old config with {} entries", licensesOld.size());
            if (!licensesOld.isEmpty())
            {
                saveSettings(licensesOld);
            }
        }
        persistentSettings.saveProperty(LICENSE_KEY, null);
    }

    private void saveSettings(List<License> licenses)
    {
        String indexes = IntStream.range(1, licenses.size())
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(","));
        persistentSettings.saveProperty(LICENSE_SET, indexes);
        for (int i = 0; i < licenses.size(); i++)
        {
            License license = licenses.get(i);
            String idxProp = "." + i + ".";
            persistentSettings.saveProperty(LICENSE_SET + idxProp + FIELD_NAME, license.getName());
            persistentSettings.saveProperty(LICENSE_SET + idxProp + FIELD_ID, license.getIdentifier());
            persistentSettings.saveProperty(LICENSE_SET + idxProp + FIELD_ALLOWED, license.getAllowed().toString());
        }
        LOGGER.info("Saving the new config with {} entries", licenses.size());
    }
}
