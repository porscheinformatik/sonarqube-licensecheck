package at.porscheinformatik.sonarqube.licensecheck;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.gradle.GradleDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

public class LicenseCheckSensor implements Sensor
{
    private static final Logger LOGGER = Loggers.get(LicenseCheckSensor.class);
    private static final Set<License> AGGREGATED_LICENSES = ConcurrentHashMap.newKeySet();
    private static final Set<Dependency> AGGREGATED_DEPENDENCIES = ConcurrentHashMap.newKeySet();
    private final FileSystem fs;
    private final Configuration configuration;
    private final ValidateLicenses validateLicenses;
    private final Scanner[] scanners;

    public LicenseCheckSensor(FileSystem fs, Configuration configuration, ValidateLicenses validateLicenses,
        MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService)
    {
        this.fs = fs;
        this.configuration = configuration;
        this.validateLicenses = validateLicenses;
        this.scanners = new Scanner[]{
            new PackageJsonDependencyScanner(
                configuration.getBoolean(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITIVE_DEPS).orElse(false)),
            new MavenDependencyScanner(mavenLicenseService, mavenDependencyService),
            new GradleDependencyScanner(mavenLicenseService)};
    }

    private static void saveDependencies(SensorContext sensorContext, Set<Dependency> dependencies)
    {
        LOGGER.debug("Saving dependencies for module {}: {}", sensorContext.project(), dependencies);

        if (!dependencies.isEmpty())
        {
            sensorContext.<String>newMeasure()
                .forMetric(LicenseCheckMetrics.DEPENDENCY)
                .withValue(Dependency.createString(dependencies))
                .on(sensorContext.module())
                .save();
        }
    }

    private static void saveLicenses(SensorContext sensorContext, Set<License> licenses)
    {
        LOGGER.debug("Saving licenses for module {}: {}", sensorContext.module(), licenses);

        if (!licenses.isEmpty())
        {
            sensorContext
                .<String>newMeasure()
                .forMetric(LicenseCheckMetrics.LICENSE)
                .withValue(License.createJsonString(licenses))
                .on(sensorContext.module())
                .save();
        }
    }

    private static void saveMeasures(SensorContext sensorContext, Set<License> licenses, Set<Dependency> dependencies)
    {
        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_LICENSES)
            .withValue(licenses.size())
            .on(sensorContext.module())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_LICENSES_FORBIDDEN)
            .withValue((int) licenses.stream().filter(License::getAllowed).count())
            .on(sensorContext.module())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_DEPENDENCIES)
            .withValue(dependencies.size())
            .on(sensorContext.module())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_DEPENDENCIES_WITH_FORBIDDEN_LICENSE)
            .withValue(
                (int) dependencies.stream().filter(d -> Dependency.Status.Forbidden.equals(d.getStatus())).count())
            .on(sensorContext.module())
            .save();

        sensorContext.<Integer>newMeasure()
            .forMetric(LicenseCheckMetrics.NO_DEPENDENCIES_WITH_UNKNOWN_LICENSE)
            .withValue(
                (int) dependencies.stream().filter(d -> Dependency.Status.Unknown.equals(d.getStatus())).count())
            .on(sensorContext.module())
            .save();
    }

    @Override
    public void describe(SensorDescriptor descriptor)
    {
        descriptor.name("License Check")
            .createIssuesForRuleRepository(LicenseCheckMetrics.LICENSE_CHECK_KEY);
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
            dependencies.addAll(scanner.scan(fs.baseDir()));
        }
        InputProject project = context.project();
        Set<Dependency> validatedDependencies = validateLicenses.validateLicenses(dependencies, context);

        Set<License> usedLicenses = validateLicenses.getUsedLicenses(validatedDependencies, project);

        AGGREGATED_LICENSES.addAll(usedLicenses);
        AGGREGATED_DEPENDENCIES.addAll(validatedDependencies);

        // root module?
        if (context.project().key().equals(context.module().key()))
        {
            saveDependencies(context, AGGREGATED_DEPENDENCIES);
            saveLicenses(context, AGGREGATED_LICENSES);
            saveMeasures(context, AGGREGATED_LICENSES, AGGREGATED_DEPENDENCIES);
        }
        else
        {
            saveDependencies(context, validatedDependencies);
            saveLicenses(context, usedLicenses);
            saveMeasures(context, usedLicenses, validatedDependencies);
        }
    }
}
