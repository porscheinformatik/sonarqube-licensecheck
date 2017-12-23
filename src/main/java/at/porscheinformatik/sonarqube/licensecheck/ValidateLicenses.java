package at.porscheinformatik.sonarqube.licensecheck;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

@BatchSide
public class ValidateLicenses
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateLicenses.class);
    private final LicenseService licenseService;

    public ValidateLicenses(LicenseService licenseService)
    {
        super();
        this.licenseService = licenseService;
    }

    public Set<Dependency> validateLicenses(Set<Dependency> dependencies, Project module, SensorContext context)
    {
        for (Dependency dependency : dependencies)
        {
            if (StringUtils.isBlank(dependency.getLicense()))
            {
                licenseNotFoundIssue(module, context, dependency);
            }
            else
            {
                checkForLicenses(module, context, dependency);
            }
        }
        return dependencies;
    }

    public Set<License> getUsedLicenses(Set<Dependency> dependencies, Project module)
    {
        Set<License> usedLicenseList = new TreeSet<>();
        
        List<License> licenses = licenseService.getLicenses(module.getRoot());

        for (Dependency dependency : dependencies)
        {
            for (License license : licenses)
            {
                if (license.getIdentifier().equals(dependency.getLicense()))
                {
                    usedLicenseList.add(license);
                }
            }
        }

        return usedLicenseList;
    }

    private void checkForLicenses(Project module, SensorContext context, Dependency dependency)
    {
        for (License license : licenseService.getLicenses(module.getRoot()))
        {
            if (license.getIdentifier().equals(dependency.getLicense()))
            {
                if ("false".equals(license.getStatus()))
                {
                    LOGGER.info("Dependency "
                        + dependency.getName()
                        + " uses a not allowed licooense "
                        + dependency.getLicense());

                    NewIssue issue = context
                        .newIssue()
                        .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                            LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY))
                        .at(new DefaultIssueLocation()
                            .on(new DefaultInputModule(module.definition()))
                            .message("Dependency "
                            + dependency.getName()
                            + " uses a not allowed license "
                            + dependency.getLicense()));
                    issue.save();
                }
            }
        }
    }

    private static void licenseNotFoundIssue(Project module, SensorContext context, Dependency dependency)
    {
        if (StringUtils.isBlank(dependency.getLicense()))
        {
            LOGGER.info("No License found for Dependency " + dependency.getName());

            NewIssue issue = context
                .newIssue()
                .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                    LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY))
                .at(new DefaultIssueLocation()
                    .on(new DefaultInputModule(module.definition()))
                    .message("No License found for Dependency: " + dependency.getName()));
            issue.save();
        }
    }
}
