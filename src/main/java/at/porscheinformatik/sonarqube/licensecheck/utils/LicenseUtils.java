package at.porscheinformatik.sonarqube.licensecheck.utils;

import java.util.Map;
import java.util.regex.Pattern;

import org.sonar.api.internal.apachecommons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;

public class LicenseUtils {

    private LicenseUtils() {}

    private static final Logger LOGGER = Loggers.get(LicenseUtils.class);

    static public Dependency matchLicense(Map<Pattern, String> licenseMap, Dependency dependency) {
        if (StringUtils.isBlank(dependency.getLicense())) {
            LOGGER.info("Dependency '{}' has no license set.", dependency.getName());
            return dependency;
        }

        for (Map.Entry<Pattern, String> entry : licenseMap.entrySet()) {
            if (entry.getKey().matcher(dependency.getLicense()).matches()) {
                dependency.setLicense(entry.getValue());
                break;
            }
        }
        return dependency;
    }
    
}
