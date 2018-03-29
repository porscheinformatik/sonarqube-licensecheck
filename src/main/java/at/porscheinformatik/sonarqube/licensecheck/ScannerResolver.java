package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ScannerResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerResolver.class);

    static Scanner[] resolveScanners(File baseDir) {
        return resolveScanners(baseDir, null, null);
    }

    static Scanner[] resolveScanners(File baseDir, MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService) {
        List<Scanner> scanners = new ArrayList<Scanner>();

        if (hasPomXml(baseDir) && mavenLicenseService != null && mavenDependencyService != null) {
            LOGGER.info("Found pom.xml in baseDir -> activating maven dependency scan.");
            scanners.add(new MavenDependencyScanner(mavenLicenseService, mavenDependencyService));
        } else if (hasBuildGradle(baseDir)) {
            LOGGER.info("Found build.gradle in baseDir -> activating gradle dependency scan.");
            scanners.add(new GradleDependencyScanner(mavenLicenseService, mavenDependencyService));
        } else {
            LOGGER.warn("Found no pom.xml and no build.gradle in base dir: " + baseDir.getAbsolutePath());
        }

        scanners.add(new PackageJsonDependencyScanner());

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
