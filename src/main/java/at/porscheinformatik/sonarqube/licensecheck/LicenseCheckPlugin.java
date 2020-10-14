package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.utils.IOUtils;
import at.porscheinformatik.sonarqube.licensecheck.webservice.license.LicenseWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency.MavenDependencyWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense.MavenLicenseWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense.ProjectLicenseWs;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LicenseCheckPlugin implements Plugin
{
    private static final Logger LOGGER = Loggers.get(LicenseCheckPlugin.class);

    @Override
    public void define(Context context)
    {
        try {
            context.addExtensions(getExtensions());
        } catch (IOException e) {
            LOGGER.error("Could not define license check plugin", e);
        }
    }

    private List<?> getExtensions() throws IOException {
        return Arrays.asList(
            ValidateLicenses.class,
            LicenseCheckSensor.class,
            LicenseCheckMetrics.class,
            LicenseCheckPageDefinition.class,
            LicenseCheckRulesDefinition.class,
            LicenseService.class,
            LicenseWs.class,
            MavenDependencyService.class,
            MavenDependencyWs.class,
            MavenLicenseService.class,
            MavenLicenseWs.class,
            ProjectLicenseService.class,
            ProjectLicenseWs.class,
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ACTIVATION_KEY)
                .category("License Check")
                .subCategory("General")
                .name("Activate license check")
                .description("(De-)Activate license check at all")
                .type(PropertyType.BOOLEAN)
                .defaultValue("true")
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS)
                .category("License Check")
                .subCategory("General")
                .name("Activate npm transitive dependency check")
                .description("(De-)Activate npm transitive dependency check")
                .type(PropertyType.BOOLEAN)
                .defaultValue("false")
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_KEY)
                .category("License Check")
                .subCategory("Read-only / JSON")
                .name("Licenses")
                .description("JSON property - do not edit here! - Use Administration -> Configuration -> License check")
                .type(PropertyType.TEXT)
                //we use multi value field to "override" 4000 character limit in settings API
                .multiValues(true)
                .hidden()
                .defaultValue(IOUtils.readToString(
                    LicenseService.class.getResourceAsStream("spdx_license_list.json")).replaceAll(",", LicenseService.COMMA_PLACEHOLDER))
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY)
                .category("License Check")
                .subCategory("Read-only / JSON")
                .name("Dependencies")
                .description("JSON property - do not edit here! - Use Administration -> Configuration -> License check")
                .hidden()
                .type(PropertyType.TEXT)
                .multiValues(true)
                //TODO: Try to setup defaultValue here instead of initialize later.
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_REGEX)
                .category("License Check")
                .subCategory("Read-only / JSON")
                .name("Maven licenses")
                .description("JSON property - do not edit here! - Use Administration -> Configuration -> License check")
                .hidden()
                .type(PropertyType.TEXT)
                .multiValues(true)
                .defaultValue(IOUtils.readToString(
                    MavenLicenseService.class.getResourceAsStream(
                        "default_license_mapping.json")).replaceAll(",", LicenseService.COMMA_PLACEHOLDER))
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY)
                .category("License Check")
                .subCategory("Read-only / JSON")
                .name("Project licenses")
                .description("JSON property - do not edit here! - Use Administration -> Configuration -> License check")
                .hidden()
                .type(PropertyType.TEXT)
                .multiValues(true)
                .build());
    }
}
