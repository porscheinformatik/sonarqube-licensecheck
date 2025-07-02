package at.porscheinformatik.sonarqube.licensecheck;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY_GROOVY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY_JS;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY_KOTLIN;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY_TS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.fs.InputProject;

public class LicenseCheckSensorTest {

    public static final Set<Dependency> DEPENDENCIES = Collections.singleton(
        new Dependency("name", "1.0.0", "MIT")
    );
    private static final Set<License> LICENSES = Collections.singleton(
        new License("MIT", "MIT", true)
    );

    @Test
    public void describe() {
        Configuration configuration = createConfiguration();
        LicenseCheckSensor sensor = new LicenseCheckSensor(configuration, null, null);
        SensorDescriptor descriptor = mock(SensorDescriptor.class);
        when(descriptor.name(anyString())).thenReturn(descriptor);

        sensor.describe(descriptor);

        verify(descriptor).name("License Check");
        verify(descriptor)
            .createIssuesForRuleRepositories(
                RULE_REPO_KEY,
                RULE_REPO_KEY_JS,
                RULE_REPO_KEY_TS,
                RULE_REPO_KEY_GROOVY,
                RULE_REPO_KEY_KOTLIN,
                LicenseCheckRulesDefinition.RULE_REPO_KEY_PYTHON,
                LicenseCheckRulesDefinition.RULE_REPO_KEY_DOTNET
            );
    }

    @Test
    public void execute() throws IllegalAccessException {
        Configuration configuration = createConfiguration();
        ValidateLicenses validateLicenses = mock(ValidateLicenses.class);
        when(validateLicenses.validateLicenses(any(), any())).thenReturn(DEPENDENCIES);
        when(validateLicenses.getUsedLicenses(any(), any())).thenReturn(LICENSES);
        LicenseMappingService licenseMappingService = mock(LicenseMappingService.class);
        LicenseCheckSensor sensor = new LicenseCheckSensor(
            configuration,
            validateLicenses,
            licenseMappingService
        );
        Scanner mockScanner = mock(Scanner.class);
        when(mockScanner.scan(any())).thenReturn(DEPENDENCIES);
        Scanner[] scanners = new Scanner[] { mockScanner };
        FieldUtils.writeField(sensor, "scanners", scanners, true);
        SensorContext context = mock(SensorContext.class);
        InputProject project = mock(InputProject.class);
        InputModule module = mock(InputModule.class);
        when(module.key()).thenReturn("my-project");
        when(context.module()).thenReturn(module);
        when(project.key()).thenReturn("my-project");
        when(context.project()).thenReturn(project);
        NewMeasure measure = mock(NewMeasure.class);
        when(measure.forMetric(any())).thenReturn(measure);
        when(measure.withValue(any())).thenReturn(measure);
        when(measure.on(any())).thenReturn(measure);
        when(context.newMeasure()).thenReturn(measure);

        sensor.execute(context);

        verify(measure, times(7)).save(); // 5 metrics + dependencies + licenses
    }

    private Configuration createConfiguration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getBoolean(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS))
            .thenReturn(Optional.empty());
        when(configuration.getBoolean(LicenseCheckPropertyKeys.ACTIVATION_KEY))
            .thenReturn(Optional.of(true));
        return configuration;
    }
}
