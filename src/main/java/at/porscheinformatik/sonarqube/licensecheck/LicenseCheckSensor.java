package at.porscheinformatik.sonarqube.licensecheck;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY_GROOVY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.RULE_REPO_KEY_JS;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

public class LicenseCheckSensor implements Sensor
{
    private static final Logger LOGGER = Loggers.get(LicenseCheckSensor.class);
    private static final Set<License> AGGREGATED_LICENSES = ConcurrentHashMap.newKeySet();
    private static final Set<Dependency> AGGREGATED_DEPENDENCIES = ConcurrentHashMap.newKeySet();
    private final Configuration configuration;
    private final ValidateLicenses validateLicenses;
    private final Scanner[] scanners;

    public LicenseCheckSensor(Configuration configuration, ValidateLicenses validateLicenses,
        LicenseMappingService licenseMappingService)
    {
        this.configuration = configuration;
        this.validateLicenses = validateLicenses;
        this.scanners = new Scanner[]{
            new PackageJsonDependencyScanner(licenseMappingService,
                configuration.getBoolean(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS).orElse(false)),
            new MavenDependencyScanner(licenseMappingService),
            new GradleDependencyScanner(licenseMappingService)};
    }

    private static void saveDependencies(SensorContext sensorContext)
    {
        LOGGER.debug("Saving dependencies for module {}: {}", sensorContext.project(), LicenseCheckSensor.AGGREGATED_DEPENDENCIES);

        if (!LicenseCheckSensor.AGGREGATED_DEPENDENCIES.isEmpty())
        {
            sensorContext.<String>newMeasure()
                .forMetric(LicenseCheckMetrics.DEPENDENCY)
                .withValue(Dependency.createString(LicenseCheckSensor.AGGREGATED_DEPENDENCIES))
                .on(sensorContext.project())
                .save();
        }
    }

    private static void saveLicenses(SensorContext sensorContext)
    {
        LOGGER.debug("Saving licenses for project {}: {}", sensorContext.project(), LicenseCheckSensor.AGGREGATED_LICENSES);

        if (!LicenseCheckSensor.AGGREGATED_LICENSES.isEmpty())
        {
            sensorContext
                .<String>newMeasure()
                .forMetric(LicenseCheckMetrics.LICENSE)
                .withValue(License.createJsonString(LicenseCheckSensor.AGGREGATED_LICENSES))
                .on(sensorContext.project())
                .save();
        }
    }

    private static void saveMeasures(SensorContext sensorContext)
    {
        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_LICENSES)
            .withValue(LicenseCheckSensor.AGGREGATED_LICENSES.size())
            .on(sensorContext.project())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_LICENSES_FORBIDDEN)
            .withValue((int) LicenseCheckSensor.AGGREGATED_LICENSES.stream().filter(License::getAllowed).count())
            .on(sensorContext.project())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_DEPENDENCIES)
            .withValue(LicenseCheckSensor.AGGREGATED_DEPENDENCIES.size())
            .on(sensorContext.project())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_DEPENDENCIES_WITH_FORBIDDEN_LICENSE)
            .withValue(
                (int) LicenseCheckSensor.AGGREGATED_DEPENDENCIES.stream().filter(d -> Dependency.Status.FORBIDDEN.equals(d.getStatus())).count())
            .on(sensorContext.project())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_DEPENDENCIES_WITH_UNKNOWN_LICENSE)
            .withValue(
                (int) LicenseCheckSensor.AGGREGATED_DEPENDENCIES.stream().filter(d -> Dependency.Status.UNKNOWN.equals(d.getStatus())).count())
            .on(sensorContext.project())
            .save();
    }

    @Override
    public void describe(SensorDescriptor descriptor)
    {
        descriptor.name("License Check")
            .createIssuesForRuleRepositories(RULE_REPO_KEY, RULE_REPO_KEY_JS, RULE_REPO_KEY_GROOVY);
    }

    @Override
    public void execute(SensorContext context)
    {
        if (!configuration.getBoolean(LicenseCheckPropertyKeys.ACTIVATION_KEY).orElse(true))
        {
            LOGGER.info("Scanner is set to inactive. No scan possible.");
            return;
        }

        Set<Dependency> dependencies = new TreeSet<>();

        for (Scanner scanner : scanners)
        {
            dependencies.addAll(scanner.scan(context));
        }
        InputProject project = context.project();
        Set<Dependency> validatedDependencies = validateLicenses.validateLicenses(dependencies, context);

        Set<License> usedLicenses = validateLicenses.getUsedLicenses(validatedDependencies, project);

        AGGREGATED_LICENSES.addAll(usedLicenses);
        AGGREGATED_DEPENDENCIES.addAll(validatedDependencies);

        // root module?
        if (context.project().key().equals(context.module().key()))
        {
            saveDependencies(context);
            saveLicenses(context);
            saveMeasures(context);
        }
    }
}
