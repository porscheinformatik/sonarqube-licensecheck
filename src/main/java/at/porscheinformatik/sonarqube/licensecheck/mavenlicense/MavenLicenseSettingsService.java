package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.server.platform.PersistentSettings;

import at.porscheinformatik.sonarqube.licensecheck.utils.IOUtils;

@ServerSide
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
        initMavenLicenses();
    }

    private void initMavenLicenses()
    {
        String mavenLicenseListString = configuration.get(LICENSE_REGEX).orElse(null);

        if (mavenLicenseListString != null && !mavenLicenseListString.isEmpty())
        {
            return;
        }

        try (InputStream in = MavenLicenseSettingsService.class.getResourceAsStream("default_license_mapping.json"))
        {
            mavenLicenseListString = IOUtils.readToString(in);
            persistentSettings.getSettings().setProperty(LICENSE_REGEX, mavenLicenseListString);
            persistentSettings.saveProperty(LICENSE_REGEX, mavenLicenseListString);
        }
        catch (Exception e)
        {
            LOGGER.error("Could not load default_license_mapping.json", e);
        }
    }

    public boolean addMavenLicense(String regex, String key)
    {
        MavenLicense newMavenLicense = new MavenLicense(regex, key);

        if (!checkIfListContains(newMavenLicense))
        {
            addMavenLicense(newMavenLicense);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean addMavenLicense(MavenLicense newMavenLicense)
    {
        List<MavenLicense> mavenLicenses = mavenLicenseService.getMavenLicenseList();

        if (!mavenLicenses.contains(newMavenLicense))
        {
            mavenLicenses.add(newMavenLicense);
            saveSettings(mavenLicenses);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean checkIfListContains(MavenLicense mavenLicense)
    {
        List<MavenLicense> mavenLicenses = mavenLicenseService.getMavenLicenseList();
        return mavenLicenses.contains(mavenLicense);
    }

    public void deleteMavenLicense(String regex)
    {
        List<MavenLicense> mavenLicenses = mavenLicenseService.getMavenLicenseList();
        Iterator<MavenLicense> i = mavenLicenses.iterator();

        while (i.hasNext())
        {
            String regexFromList = i.next().getRegex().toString();
            if (regexFromList.equals(regex))
            {
                i.remove();
            }
        }
        saveSettings(mavenLicenses);
    }

    private void saveSettings(List<MavenLicense> mavenLicenses)
    {
        String mavenLicensesString = MavenLicense.createString(mavenLicenses);

        persistentSettings.getSettings().setProperty(LICENSE_REGEX, mavenLicensesString);
        persistentSettings.saveProperty(LICENSE_REGEX, mavenLicensesString);
    }

    public void sortMavenLicenses()
    {
        List<MavenLicense> mavenLicenses = mavenLicenseService.getMavenLicenseList();
        Collections.sort(mavenLicenses);
        saveSettings(mavenLicenses);
    }
}
