package at.porscheinformatik.sonarqube.licensecheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;

import at.porscheinformatik.sonarqube.licensecheck.dependency.AllowedDependency;
import at.porscheinformatik.sonarqube.licensecheck.dependency.DependencyService;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

@BatchSide
public class ValidateLicenses
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateLicenses.class);
    private final LicenseService licenseService;
    private final DependencyService dependencyService;

    public ValidateLicenses(LicenseService licenseService, DependencyService dependencyService)
    {
        super();
        this.licenseService = licenseService;
        this.dependencyService = dependencyService;
    }

    public Set<Dependency> validateLicenses(Set<Dependency> dependencies, Project module, SensorContext context)
    {
        if (!dependencies.isEmpty() && dependencies.toString().contains(" "))
        {
            for (Dependency dependency : dependencies)
            {
                if (" ".equals(dependency.getLicense()))
                {
                    checkAllowedDependencies(module, context, dependency);
                    licenseNotFoundIssue(module, context, dependency);
                }
            }
        }
        return dependencies;
    }

    public List<License> getAllowedLicenses()
    {
        List<License> allowedLicensesList = new ArrayList<>();
        for (License license : licenseService.getLicenses())
        {
            if ("true".equals(license.getStatus()))
            {
                allowedLicensesList.add(new License(license.getName(), license.getIdentifier(), license.getStatus()));
            }
        }
        return allowedLicensesList;
    }

    public Set<License> getUsedLicenses(Set<Dependency> dependencies)
    {
        Set<License> usedLicenseList = new TreeSet<>();
        for (Dependency dependency : dependencies)
        {
            for (License license : licenseService.getLicenses())
            {
                usedLicenseList = checkListForLicense(dependency, license, usedLicenseList);
            }
        }
        return usedLicenseList;
    }

    private static Set<License> checkListForLicense(Dependency dependency, License license,
        Set<License> usedLicenseList)
    {
        if (dependency.getLicense() != null)
        {
            if (dependency.getLicense().equals(license.getIdentifier()))
            {
                usedLicenseList.add(new License(license.getName(), license.getIdentifier(), license.getStatus()));
            }
        }
        else
        {
            dependency.setLicense(" ");
        }

        return usedLicenseList;
    }

    private void checkAllowedDependencies(Project module, SensorContext context, Dependency dependency)
    {
        for (AllowedDependency allowedDependency : dependencyService.getAllowedDependencies())
        {
            String matchString = allowedDependency.getKey().replace("\\.", ".");
            if (dependency.getName().matches(matchString))
            {
                checkForLicenses(module, context, allowedDependency, dependency);
            }
        }
    }

    private void checkForLicenses(Project module, SensorContext context, AllowedDependency allowedDependency,
        Dependency dependency)
    {
        for (License license : licenseService.getLicenses())
        {
            if (license.getIdentifier().equals(allowedDependency.getLicense()))
            {
                dependency.setLicense(allowedDependency.getLicense());
                if ("false".equals(license.getStatus()))
                {
                    LOGGER.info("Dependency " + dependency.getName() + " uses a not allowed license "
                        + dependency.getLicense());

                    NewIssue issue = context.newIssue()
                        .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                            LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY))
                        .at(new DefaultIssueLocation().on(new DefaultInputModule(module.getKey()))
                            .message("Dependency " + dependency.getName() + " uses a not allowed license "
                                + dependency.getLicense()));
                    issue.save();
                }
            }
        }
    }

    private static void licenseNotFoundIssue(Project module, SensorContext context, Dependency dependency)
    {
        if (" ".equals(dependency.getLicense()))
        {
            LOGGER.info("No License found for Dependency " + dependency.getName());

            NewIssue issue =
                context.newIssue().forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                    LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY))
                    .at(new DefaultIssueLocation().on(new DefaultInputModule(module.getKey()))
                        .message("No License found for Dependency: " + dependency.getName()));
            issue.save();
        }
    }
}
