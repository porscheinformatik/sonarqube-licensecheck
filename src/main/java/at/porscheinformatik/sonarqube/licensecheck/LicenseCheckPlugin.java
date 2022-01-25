package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping;
import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMappingService;
import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMappingSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMapping;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.web.LicenseCheckWebService;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyFieldDefinition;

import java.util.Arrays;
import java.util.List;

public class LicenseCheckPlugin implements Plugin
{
    private static final String LICENSE_ID_DESCRIPTION = "The identifier of the license (e.g. GPL-3.0)";

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
            DependencyMappingService.class,
            DependencyMappingSettingsService.class,
            LicenseMappingService.class,
            LicenseMappingSettingsService.class,
            ProjectLicenseService.class,
            ProjectLicenseSettingsService.class,
            LicenseCheckWebService.class,
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_SET)
                .category(LicenseCheckPropertyKeys.CATEGORY)
                .type(PropertyType.PROPERTY_SET)
                .name("Licenses")
                .description("List of known licenses with allowed status")
                .fields(
                    PropertyFieldDefinition.build(License.FIELD_ID)
                        .name("Identifier")
                        .description(LICENSE_ID_DESCRIPTION)
                        .type(PropertyType.STRING).build(),
                    PropertyFieldDefinition.build(License.FIELD_NAME)
                        .name("Name")
                        .description("The name of the license (e.g. GNU General Public License v3.0 only).")
                        .type(PropertyType.STRING).build(),
                    PropertyFieldDefinition.build(License.FIELD_ALLOWED)
                        .name("Allowed")
                        .description("If the license is allowed to use")
                        .type(PropertyType.BOOLEAN).build()
                )
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.DEPENDENCY_MAPPING)
                .category(LicenseCheckPropertyKeys.CATEGORY)
                .type(PropertyType.PROPERTY_SET)
                .name("Dependency Mapping")
                .description("Maps a dependency name/key (with regex) to a license")
                .fields(
                    PropertyFieldDefinition.build(DependencyMapping.FIELD_KEY)
                        .name("Dependency")
                        .description("A regular expression to match against the dependency key.")
                        .type(PropertyType.REGULAR_EXPRESSION).build(),
                    PropertyFieldDefinition.build(DependencyMapping.FIELD_LICENSE)
                        .name("License Identifier")
                        .description(LICENSE_ID_DESCRIPTION)
                        .type(PropertyType.STRING).build(),
                    PropertyFieldDefinition.build(DependencyMapping.FIELD_OVERWRITE)
                        .name("Overwrite License")
                        .description("Overwrite the license defined by the dependency.")
                        .type(PropertyType.BOOLEAN).build()
                )
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_MAPPING)
                .category(LicenseCheckPropertyKeys.CATEGORY)
                .type(PropertyType.PROPERTY_SET)
                .name("License Mapping")
                .description("Maps a license name (with regex) to a license")
                .fields(
                    PropertyFieldDefinition.build(LicenseMapping.FIELD_REGEX)
                        .name("License name")
                        .description("A regular expression to match against the license name.")
                        .type(PropertyType.REGULAR_EXPRESSION).build(),
                    PropertyFieldDefinition.build(LicenseMapping.FIELD_LICENSE)
                        .name("License Identifier")
                        .description(LICENSE_ID_DESCRIPTION)
                        .type(PropertyType.STRING).build()
                )
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.PROJECT_LICENSE_SET)
                .category(LicenseCheckPropertyKeys.CATEGORY)
                .type(PropertyType.PROPERTY_SET)
                .name("Project Licenses")
                .description("Allow/disallow licences for specific projects.")
                .fields(
                    PropertyFieldDefinition.build(ProjectLicense.FIELD_PROJECT_KEY)
                        .name("Project key")
                        .description("The project key")
                        .type(PropertyType.REGULAR_EXPRESSION).build(),
                    PropertyFieldDefinition.build(ProjectLicense.FIELD_LICENSE)
                        .name("License Identifier")
                        .description(LICENSE_ID_DESCRIPTION)
                        .type(PropertyType.STRING).build(),
                    PropertyFieldDefinition.build(ProjectLicense.FIELD_ALLOWED)
                        .name("Allowed")
                        .description("If the license is allowed to use")
                        .type(PropertyType.BOOLEAN).build()
                )
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_KEY)
                .hidden()
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY)
                .hidden()
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.LICENSE_REGEX)
                .hidden()
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.PROJECT_LICENSE_KEY)
                .hidden()
                .type(PropertyType.TEXT)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS)
                .category(LicenseCheckPropertyKeys.CATEGORY)
                .name("NPM Transitive Dependencies")
                .description("Scan transitive dependencies for NPM packages")
                .type(PropertyType.BOOLEAN)
                .build(),
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ACTIVATION_KEY)
                .category(LicenseCheckPropertyKeys.CATEGORY)
                .name("Activate")
                .description("Activate license check")
                .type(PropertyType.BOOLEAN)
                .defaultValue("true")
                .build());
    }
}
