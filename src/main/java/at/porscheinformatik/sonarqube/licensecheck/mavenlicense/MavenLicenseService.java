package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;

@ServerSide
@BatchSide
public class MavenLicenseService
{
    private final Settings settings;

    public MavenLicenseService(Settings settings)
    {
        super();
        this.settings = settings;
    }

    public List<MavenLicense> getMavenLicenseList()
    {
        List<MavenLicense> mavenLicenseList = new ArrayList<>();

        String mavenLicenseString = settings.getString(LICENSE_REGEX);
        if (StringUtils.isNotEmpty(mavenLicenseString))
        {
            String[] mavenLicenseEntries = mavenLicenseString.split(";");
            for (String mavenLicenseEntry : mavenLicenseEntries)
            {
                String[] mavenLicenseEntryParts = mavenLicenseEntry.split("~");
                mavenLicenseList.add(new MavenLicense(mavenLicenseEntryParts[0], mavenLicenseEntryParts[1]));
            }
        }

        return mavenLicenseList;
    }

    public Map<Pattern, String> getLicenseMap()
    {
        Map<Pattern, String> licenseMap = new HashMap<>();
        for (MavenLicense license : getMavenLicenseList())
        {
            licenseMap.put(license.getLicenseNameRegEx(), license.getLicenseKey());
        }
        return licenseMap;
    }
}
