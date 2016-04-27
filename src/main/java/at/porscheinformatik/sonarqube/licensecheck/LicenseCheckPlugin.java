package at.porscheinformatik.sonarqube.licensecheck;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;

import at.porscheinformatik.sonarqube.licensecheck.dependency.DependencyService;
import at.porscheinformatik.sonarqube.licensecheck.dependency.DependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.widget.DependencyCheckWidget;
import at.porscheinformatik.sonarqube.licensecheck.widget.DependencyCheckWidgetValidator;
import at.porscheinformatik.sonarqube.licensecheck.widget.UsedLicensesWidget;

public class LicenseCheckPlugin extends SonarPlugin
{
    @Override
    public List getExtensions()
    {
        return Arrays.asList(
            LicenseCheckSensor.class,
            LicenseCheckMetrics.class,
            DependencyCheckWidget.class,
            UsedLicensesWidget.class,
            LicenseCheckConfigurationPage.class,
            LicenseService.class,
            LicenseSettingsService.class,
            DependencyService.class,
            DependencySettingsService.class,
            ValidateLicenses.class,
            DependencyCheckWidgetValidator.class,
            LicenseCheckMeasureComputer.class,
            LicenseCheckRulesDefinition.class,
            LicenseCheckPropertyKeys.class,
            PropertyDefinition.builder(LicenseCheckPropertyKeys.ACTIVATION_KEY)
                .category("License Check")
                .name("Activate")
                .description("Activate license check")
                .type(PropertyType.BOOLEAN)
                .defaultValue("true")
                .build());
    }
}
