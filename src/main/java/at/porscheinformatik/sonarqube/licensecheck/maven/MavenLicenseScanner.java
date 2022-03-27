package at.porscheinformatik.sonarqube.licensecheck.maven;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import org.codehaus.mojo.license.download.LicenseSummaryReader;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * License scanner re-using <code>licenses.xml</code> files of <a href="https://www.mojohaus.org/license-maven-plugin/index.html"><code>license-maven-plugin</code></a>.
 * @author criztovyl
 */
public class MavenLicenseScanner implements Scanner {

    private static final Logger LOGGER = Loggers.get(MavenLicenseScanner.class);

    LicenseMappingService licenseMappingService;

    public MavenLicenseScanner(LicenseMappingService licenseMappingService) {
        this.licenseMappingService = licenseMappingService;
    }

    @Override
    public Set<Dependency> scan(SensorContext context) {

        File licenseReport = new File(context.fileSystem().baseDir(), "target/generated-resources/licenses.xml");

        if(!licenseReport.exists()){
            LOGGER.info(String.format("No licenses.xml file found in %s - skipping Maven license scan", licenseReport));
        }

        try {

            // Domain info:
            // 1 LicenseSummary per module
            // 1 ProjectLicenseInfo per dependency
            // 1 ProjectLicense per dependency license

            return LicenseSummaryReader.parseLicenseSummary(licenseReport).stream().map(projectLicenseInfo -> {

                String dependencyName = String.format("%s:%s", projectLicenseInfo.getGroupId(), projectLicenseInfo.getArtifactId());

                // use first dependency only
                // mirrors the behaviour of MavenDependencyScanner
                String licenseName = projectLicenseInfo.getLicenses().stream()
                    .map(lic -> licenseMappingService.mapLicense(lic.getName()))
                    .findFirst()
                    .orElse(null);

                Dependency dependency = new Dependency(dependencyName, projectLicenseInfo.getVersion(), licenseName, LicenseCheckRulesDefinition.LANG_JAVA);
                dependency.setInputComponent(context.project());

                return dependency;

            }).collect(Collectors.toSet());

        } catch (IOException | ParserConfigurationException | SAXException e) {

            LOGGER.warn(String.format("Could not parse dependencies from license summary at \"%s\"!", licenseReport), e);

            return Collections.emptySet();

        }
    }
}
