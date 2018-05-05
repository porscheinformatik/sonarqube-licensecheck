package at.porscheinformatik.sonarqube.licensecheck.gradle.license;


import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LicenseResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseResolver.class);

    private final List<MavenDependency> mavenDependencies;

    public LicenseResolver(MavenDependencyService mavenDependencyService) {
        this.mavenDependencies = mavenDependencyService != null ?
            mavenDependencyService.getMavenDependencies() : null;
    }

    public String byPackage(String packageName) {
        if (mavenDependencies != null) {
            String allowedDependency = findDependency(packageName);
            if (allowedDependency != null) {
                return allowedDependency;
            }
        }
        LOGGER.debug("Could not find matching license for package name: " + packageName);
        return "";
    }

    private String findDependency(String packageName) {
        for (MavenDependency allowedDependency : mavenDependencies) {
            String matchString = allowedDependency.getKey();
            if (packageName.matches(matchString)) {
                return allowedDependency.getLicense();
            }
        }
        return null;
    }
}
