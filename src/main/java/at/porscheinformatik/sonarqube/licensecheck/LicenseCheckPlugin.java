package at.porscheinformatik.sonarqube.licensecheck;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.config.PropertyDefinition;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.license.LicenseWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency.MavenDependencyWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense.MavenLicenseWs;
import at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense.ProjectLicenseWs;

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
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ACTIVATION_KEY)
                .category("License Check")
                .name("Activate")
                .description("Activate license check")
                .type(PropertyType.BOOLEAN)
                .defaultValue("true")
                .build());
    }

    static ProjectDefinition getRootProject(ProjectDefinition definition)
    {
        while (definition != null && definition.getParent() != null && !definition.equals(definition.getParent()))
        {
            definition = definition.getParent();
        }
        return definition;
    }
}
