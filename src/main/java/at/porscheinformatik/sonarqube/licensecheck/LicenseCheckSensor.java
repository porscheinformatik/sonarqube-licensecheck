package at.porscheinformatik.sonarqube.licensecheck;

import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

public class LicenseCheckSensor implements Sensor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseCheckSensor.class);

    private final FileSystem fs;
    private final Settings settings;
    private final ValidateLicenses validateLicenses;
    private final Scanner[] scanners;

    public LicenseCheckSensor(FileSystem fs, Settings settings, ValidateLicenses validateLicenses,
        MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService)
    {
        this.fs = fs;
        this.settings = settings;
        this.validateLicenses = validateLicenses;
        this.scanners = new Scanner[]{
            new PackageJsonDependencyScanner(),
            new MavenDependencyScanner(mavenLicenseService, mavenDependencyService)};
    }

    @Override
    public boolean shouldExecuteOnProject(Project project)
    {
        return true;
    }

    @Override
    public void analyse(Project module, SensorContext context)
    {
        if (settings.getBoolean(LicenseCheckPropertyKeys.ACTIVATION_KEY))
        {
            String mavenProjectDependencies = settings.getString("sonar.maven.projectDependencies");
            Set<Dependency> dependencies = new TreeSet<>();

            for (Scanner scanner : scanners)
            {
                dependencies.addAll(scanner.scan(fs.baseDir(), mavenProjectDependencies));
            }

            Set<Dependency> validatedDependencies = validateLicenses.validateLicenses(dependencies, module, context);

            Set<License> usedLicenses = validateLicenses.getUsedLicenses(validatedDependencies, module);

            saveDependencies(context, validatedDependencies);
            saveLicenses(context, usedLicenses);
        }
        else
        {
            LOGGER.info("Scanner is set to inactive. No scan possible.");
        }
    }

    private static void saveDependencies(SensorContext sensorContext, Set<Dependency> dependencies)
    {
        if (!dependencies.isEmpty())
        {
            sensorContext.saveMeasure(
                new Measure<String>(LicenseCheckMetrics.INPUTDEPENDENCY, Dependency.createString(dependencies)));
        }
    }

    private static void saveLicenses(SensorContext sensorContext, Set<License> licenses)
    {
        if (!licenses.isEmpty())
        {
            sensorContext
                .saveMeasure(new Measure<String>(LicenseCheckMetrics.INPUTLICENSE, License.createString(licenses)));
        }
    }
}
