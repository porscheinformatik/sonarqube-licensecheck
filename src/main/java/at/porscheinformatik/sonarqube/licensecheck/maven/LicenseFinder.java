package at.porscheinformatik.sonarqube.licensecheck.maven;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LicenseFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseFinder.class);

    private LicenseFinder() {}

    public static List<License> getLicenses(
        File filePath,
        String userSettings,
        String globalSettings
    ) {
        try {
            Model model = new MavenXpp3Reader().read(new FileInputStream(filePath));

            if (!model.getLicenses().isEmpty()) {
                return model.getLicenses();
            } else {
                if (model.getParent() != null) {
                    Parent parent = model.getParent();
                    Dependency dependency = new Dependency(
                        parent.getGroupId() + ":" + parent.getArtifactId(),
                        parent.getVersion(),
                        null,
                        LicenseCheckRulesDefinition.LANG_JAVA
                    );
                    return getLicenses(
                        DirectoryFinder.getPomPath(
                            dependency,
                            DirectoryFinder.getMavenRepsitoryDir(userSettings, globalSettings)
                        ),
                        userSettings,
                        globalSettings
                    );
                } else {
                    return Collections.emptyList();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not parse Maven POM " + filePath, e);
            return Collections.emptyList();
        }
    }
}
