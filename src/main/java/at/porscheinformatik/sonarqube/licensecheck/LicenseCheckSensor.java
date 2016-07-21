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
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.maven.MavenDependencyScanner;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

public class LicenseCheckSensor implements Sensor
{
    private final FileSystem fs;
    private final Settings settings;
    private final ValidateLicenses validateLicenses;
    private final LicenseService licenseService;
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseCheckSensor.class);

    public LicenseCheckSensor(FileSystem fs, Settings settings, ValidateLicenses validateLicenses,
        LicenseService licenseService)
    {
        this.fs = fs;
        this.settings = settings;
        this.validateLicenses = validateLicenses;
        this.licenseService = licenseService;
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

            Scanner[] scanners =
                {
                    new PackageJsonDependencyScanner(),
                    new MavenDependencyScanner(licenseService)
                };

            for (Scanner scanner : scanners)
            {
                dependencies.addAll(scanner.scan(fs.baseDir(), mavenProjectDependencies));
            }

            Set<Dependency> validatedDependencies = validateLicenses.validateLicenses(dependencies, module, context);
            Set<License> usedLicenses = validateLicenses.getUsedLicenses(validatedDependencies);

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
            StringBuilder dependencyString = new StringBuilder();
            for (Dependency dependency : dependencies)
            {
                dependencyString.append(dependency.getName()).append("~");
                dependencyString.append(dependency.getVersion()).append("~");
                dependencyString.append(dependency.getLicense()).append(";");
            }
            sensorContext
                .saveMeasure(new Measure<String>(LicenseCheckMetrics.INPUTDEPENDENCY, dependencyString.toString()));
        }
    }

    private static void saveLicenses(SensorContext sensorContext, Set<License> licenses)
    {
        if (!licenses.isEmpty())
        {
            StringBuilder licenseString = new StringBuilder();
            for (License license : licenses)
            {
                licenseString.append(license.getIdentifier()).append("~");
                licenseString.append(license.getName()).append("~");
                licenseString.append(license.getStatus()).append(";");
            }
            sensorContext.saveMeasure(new Measure<String>(LicenseCheckMetrics.INPUTLICENSE, licenseString.toString()));
        }
    }
}
