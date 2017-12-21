package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        return MavenLicense.fromString(settings.getString(LICENSE_REGEX));
    }

    public Map<Pattern, String> getLicenseMap()
    {
        Map<Pattern, String> licenseMap = new HashMap<>();
        for (MavenLicense license : getMavenLicenseList())
        {
            licenseMap.put(license.getRegex(), license.getLicense());
        }
        return licenseMap;
    }
}
