package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;
import org.sonar.api.config.Configuration;

import java.io.*;
import java.util.ArrayList;

class ScannerResolver {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ScannerResolver.class);

    static Scanner[] resolveScanners(Configuration configuration, File baseDir, MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService) {
        java.util.List<Scanner> scanners = new ArrayList<Scanner>();

        if (hasPomXml(baseDir) && mavenLicenseService != null && mavenDependencyService != null) {
            LOGGER.info("Found pom.xml in baseDir -> activating maven dependency scan.");
            scanners.add(new MavenDependencyScanner(mavenLicenseService, mavenDependencyService));
        } else if (hasBuildGradle(baseDir)) {
            LOGGER.info("Found build.gradle in baseDir -> activating gradle dependency scan.");
            scanners.add(new GradleDependencyScanner(mavenLicenseService));
        } else {
            LOGGER.warn("Found no pom.xml and no build.gradle in base dir: " + baseDir.getAbsolutePath());
        }

        scanners.add(new PackageJsonDependencyScanner(configuration.getBoolean(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS).orElse(false)));

        Scanner[] scannerArray = new Scanner[scanners.size()];
        return scanners.toArray(scannerArray);
    }

    private static boolean hasPomXml(File baseDir) {
        return hasFile("pom.xml", baseDir);
    }

    private static boolean hasBuildGradle(File baseDir) {
        return hasFile("build.gradle", baseDir);
    }

    private static boolean hasFile(String fileName, File dir) {
        return new File(dir, fileName).exists();
    }
}
