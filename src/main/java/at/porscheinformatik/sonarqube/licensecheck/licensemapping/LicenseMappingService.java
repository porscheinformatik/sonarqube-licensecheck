package at.porscheinformatik.sonarqube.licensecheck.licensemapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMapping.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMapping.FIELD_REGEX;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

@ServerSide
@ScannerSide
public class LicenseMappingService
{
    private final Configuration configuration;

    /** Holding license map on scanner side */
    private static transient Map<Pattern, String> LICENSE_MAP;

    public LicenseMappingService(Configuration configuration)
    {
        super();
        this.configuration = configuration;
    }

    public List<LicenseMapping> getLicenseMappingList()
    {
        return Arrays.stream(configuration.getStringArray(LICENSE_MAPPING))
            .map(idx -> {
                String idxProp = "." + idx + ".";
                String licenseRegex =
                    configuration.get(LICENSE_MAPPING + idxProp + FIELD_REGEX).orElse("");
                String licenseId =
                    configuration.get(LICENSE_MAPPING + idxProp + FIELD_LICENSE).orElse(null);
                return new LicenseMapping(licenseRegex, licenseId);
            }).collect(Collectors.toList());
    }

    /**
     * @deprecated use {@link #getLicenseMappingList()} instead
     */
    @Deprecated
    public List<LicenseMapping> getLicenseMappingListOld()
    {
        return LicenseMapping.fromString(configuration.get(LICENSE_REGEX).orElse(null));
    }

    public Map<Pattern, String> getLicenseMap()
    {
        if (LICENSE_MAP != null)
        {
            return LICENSE_MAP;
        }

        LICENSE_MAP = new HashMap<>();
        for (LicenseMapping license : getLicenseMappingList())
        {
            LICENSE_MAP.put(license.getRegex(), license.getLicense());
        }
        return LICENSE_MAP;
    }

    public String mapLicense(String licenseName)
    {
        if (StringUtils.isBlank(licenseName))
        {
            return licenseName;
        }

        Map<Pattern, String> licenseMap = getLicenseMap();
        for (Map.Entry<Pattern, String> entry : licenseMap.entrySet())
        {
            if (entry.getKey().matcher(licenseName).matches())
            {
                return entry.getValue();
            }
        }
        return licenseName;
    }
}
