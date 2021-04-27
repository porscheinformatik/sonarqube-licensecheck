package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.license.LicenseWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency.MavenDependencyWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense.MavenLicenseWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense.ProjectLicenseWs;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

import java.util.Arrays;
import java.util.List;

public class LicenseCheckPlugin implements Plugin
{
    @Override
    public void define(Context context)
    {
        context.addExtensions(getExtensions());
    }

    private List<?> getExtensions()
    {
        return Arrays.asList(
            ValidateLicenses.class,
            LicenseCheckSensor.class,
            LicenseCheckMetrics.class,
            LicenseCheckPageDefinition.class,
            LicenseCheckRulesDefinition.class,
            LicenseService.class,
            LicenseSettingsService.class,
            LicenseWs.class,
            MavenDependencyService.class,
            MavenDependencySettingsService.class,
            MavenDependencyWs.class,
            MavenLicenseService.class,
            MavenLicenseSettingsService.class,
            MavenLicenseWs.class,
            ProjectLicenseService.class,
            ProjectLicenseSettingsService.class,
            ProjectLicenseWs.class,
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_KEY)
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY)
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_REGEX)
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY)
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS)
                .type(PropertyType.BOOLEAN)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ACTIVATION_KEY)
                .category("License Check")
                .name("Activate")
                .description("Activate license check")
                .type(PropertyType.BOOLEAN)
                .defaultValue("true")
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.CUSTOM_LICENSE_MAPPINGS)
                .category("License Check")
                .name("Custom Mapping")
                .description(
                    "Explicitly specify licenses for dependencies where licenses can not be resolved automatically.")
                .type(PropertyType.TEXT).build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.FORCED_LICENSE_MAPPINGS)
                .category("License Check")
                .name("Forced Mapping")
                .description("Explicitly force licenses for dependencies where faulty license identifiers are found.")
                .type(PropertyType.TEXT).build());
    }
}
