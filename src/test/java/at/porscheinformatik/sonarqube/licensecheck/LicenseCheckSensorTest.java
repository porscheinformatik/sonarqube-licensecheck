package at.porscheinformatik.sonarqube.licensecheck;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.fs.InputProject;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;

public class LicenseCheckSensorTest
{

    public static final Set<Dependency> DEPENDENCIES = Collections.singleton(new Dependency("name", "1.0.0", "MIT"));
    private static final Set<License> LICENSES = Collections.singleton(new License("MIT", "MIT", true));

    @Test
    public void describe()
    {
        Configuration configuration = createConfiguration();
        LicenseCheckSensor sensor = new LicenseCheckSensor(null, configuration, null, null);
        SensorDescriptor descriptor = mock(SensorDescriptor.class);
        when(descriptor.name(anyString())).thenReturn(descriptor);

        sensor.describe(descriptor);

        verify(descriptor).name(anyString());
        verify(descriptor).createIssuesForRuleRepositories(anyVararg());
    }

    @Test
    public void execute() throws IllegalAccessException
    {
        FileSystem fs = mock(FileSystem.class);
        Configuration configuration = createConfiguration();
        ValidateLicenses validateLicenses = mock(ValidateLicenses.class);
        when(validateLicenses.validateLicenses(any(), any())).thenReturn(DEPENDENCIES);
        when(validateLicenses.getUsedLicenses(any(), any())).thenReturn(LICENSES);
        LicenseMappingService licenseMappingService = mock(LicenseMappingService.class);
        LicenseCheckSensor sensor = new LicenseCheckSensor(fs, configuration, validateLicenses, licenseMappingService);
        Scanner mockScanner = mock(Scanner.class);
        when(mockScanner.scan(any())).thenReturn(DEPENDENCIES);
        Scanner[] scanners = new Scanner[]{mockScanner};
        FieldUtils.writeField(sensor, "scanners", scanners, true);
        SensorContext context = mock(SensorContext.class);
        InputProject project = mock(InputProject.class);
        when(project.key()).thenReturn("myproject");
        InputModule module = mock(InputModule.class);
        when(module.key()).thenReturn("myproject");
        when(context.project()).thenReturn(project);
        when(context.module()).thenReturn(module);
        NewMeasure measure = mock(NewMeasure.class);
        when(measure.forMetric(any())).thenReturn(measure);
        when(measure.withValue(any())).thenReturn(measure);
        when(measure.on(any())).thenReturn(measure);
        when(context.newMeasure()).thenReturn(measure);

        sensor.execute(context);

        verify(measure, times(7)).save(); // 5 metrics + dependencies + licenses
    }

    @NotNull
    private Configuration createConfiguration()
    {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getBoolean(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS))
            .thenReturn(Optional.empty());
        when(configuration.getBoolean(LicenseCheckPropertyKeys.ACTIVATION_KEY)).thenReturn(Optional.of(true));
        return configuration;
    }
}
