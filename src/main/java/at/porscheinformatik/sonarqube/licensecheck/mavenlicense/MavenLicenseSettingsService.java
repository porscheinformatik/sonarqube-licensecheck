package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

@ServerSide
public class MavenLicenseSettingsService
{
    /** This is not official API */
    private final PersistentSettings persistentSettings;
    private final Settings settings;
    private final MavenLicenseService mavenLicenseService;

    public MavenLicenseSettingsService(PersistentSettings persistentSettings, Settings settings,
        MavenLicenseService mavenLicenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.settings = settings;
        this.mavenLicenseService = mavenLicenseService;
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

        settings.setProperty(LICENSE_REGEX, mavenLicensesString);
        persistentSettings.saveProperty(LICENSE_REGEX, mavenLicensesString);
    }

    public void sortMavenLicenses()
    {
        List<MavenLicense> mavenLicenses = mavenLicenseService.getMavenLicenseList();
        Collections.sort(mavenLicenses);
        saveSettings(mavenLicenses);
    }
}
