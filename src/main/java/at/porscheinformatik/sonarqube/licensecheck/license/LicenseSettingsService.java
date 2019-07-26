package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

import at.porscheinformatik.sonarqube.licensecheck.utils.IOUtils;

@ServerSide
public class LicenseSettingsService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseSettingsService.class);

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final Configuration configuration;
    private final LicenseService licenseService;

    public LicenseSettingsService(PersistentSettings persistentSettings, Configuration configuration, LicenseService licenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.configuration = configuration;
        this.licenseService = licenseService;

        initSpdxLicences();
    }

    public String getLicensesID()
    {
        List<License> licenses = licenseService.getLicenses();

        StringBuilder licenseString = new StringBuilder();
        for (License license : licenses)
        {
            licenseString.append(license.getIdentifier()).append(";");
        }
        return licenseString.toString();
    }

    public boolean addLicense(String name, String identifier, String status)
    {
        License newLicense = new License(name, identifier, status);
        return addLicense(newLicense);
    }

    public boolean addLicense(License newLicense)
    {
        List<License> licenses = licenseService.getLicenses();

        if (listContains(newLicense, licenses))
        {
            return false;
        }

        licenses.add(newLicense);
        saveSettings(licenses);

        return true;
    }

    private boolean listContains(License newLicense, List<License> licenses)
    {
        for (License license : licenses)
        {
            if (newLicense.getIdentifier().equals(license.getIdentifier()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean deleteLicense(String id)
    {
        List<License> licenses = License.fromString(configuration.get(LICENSE_KEY).orElse(null));
        List<License> newLicenseList = new ArrayList<>();
        boolean found = false;
        for (License license : licenses)
        {
            if (id.equals(license.getIdentifier()))
            {
                found = true;
            }
            else
            {
                newLicenseList.add(license);
            }
        }

        if (found)
        {
            saveSettings(newLicenseList);
        }

        return found;
    }

    public boolean updateLicense(final String id, final String newName, final String newStatus)
    {
        List<License> licenses = licenseService.getLicenses();

        for (License license : licenses)
        {
            if (id.equals(license.getIdentifier()))
            {
                license.setName(newName);
                license.setStatus(newStatus);
                saveSettings(licenses);
                return true;
            }
        }
        return false;
    }

    private void saveSettings(List<License> licenseList)
    {
        Collections.sort(licenseList);
        String licenseJson = License.createString(licenseList);
        persistentSettings.getSettings().setProperty(LICENSE_KEY, licenseJson);
        persistentSettings.saveProperty(LICENSE_KEY, licenseJson);
    }

    private void initSpdxLicences()
    {
        String licenseJson = configuration.get(LICENSE_KEY).orElse(null);

        if (licenseJson != null && !licenseJson.isEmpty())
        {
            return;
        }

        try (InputStream inputStream = LicenseSettingsService.class.getResourceAsStream("spdx_license_list.json");)
        {
            String spdxLicenseListJson = IOUtils.readToString(inputStream);
            persistentSettings.getSettings().setProperty(LICENSE_KEY, spdxLicenseListJson);
            persistentSettings.saveProperty(LICENSE_KEY, spdxLicenseListJson);
        }
        catch (Exception e)
        {
            LOGGER.error("Could not load spdx_license_list.json", e);
        }
    }
}
