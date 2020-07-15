package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

@ServerSide
@ScannerSide
public class MavenLicenseService
{
    private final Configuration configuration;

    public MavenLicenseService(Configuration configuration)
    {
        super();
        this.configuration = configuration;
    }

    public List<MavenLicense> getMavenLicenseList()
    {
        return MavenLicense.fromString(configuration.get(LICENSE_REGEX).orElse(null));
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
