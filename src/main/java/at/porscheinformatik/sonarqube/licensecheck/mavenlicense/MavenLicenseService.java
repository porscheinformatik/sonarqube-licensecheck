package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.MAVEN_LICENSE_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicense.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicense.FIELD_REGEX;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        return Arrays.stream(configuration.getStringArray(MAVEN_LICENSE_MAPPING))
            .map(idx -> {
                String idxProp = "." + idx + ".";
                String licenseRegex =
                    configuration.get(MAVEN_LICENSE_MAPPING + idxProp + FIELD_REGEX).orElse("");
                String licenseId =
                    configuration.get(MAVEN_LICENSE_MAPPING + idxProp + FIELD_LICENSE).orElse(null);
                return new MavenLicense(licenseRegex, licenseId);
            }).collect(Collectors.toList());
    }

    /**
     * @deprecated use {@link #getMavenLicenseList()} instead
     */
    @Deprecated
    public List<MavenLicense> getMavenLicenseListOld()
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
