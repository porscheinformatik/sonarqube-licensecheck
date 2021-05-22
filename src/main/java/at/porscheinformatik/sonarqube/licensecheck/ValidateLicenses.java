package at.porscheinformatik.sonarqube.licensecheck;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.StringUtils;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

@ScannerSide
public class ValidateLicenses
{
    private static final Logger LOGGER = Loggers.get(ValidateLicenses.class);
    private final LicenseService licenseService;

    public ValidateLicenses(LicenseService licenseService)
    {
        super();
        this.licenseService = licenseService;
    }

    public Set<Dependency> validateLicenses(Set<Dependency> dependencies, SensorContext context)
    {
        for (Dependency dependency : dependencies)
        {
            dependency.setStatus(Dependency.Status.Allowed);

            if (StringUtils.isBlank(dependency.getLicense()))
            {
                licenseNotFoundIssue(context, dependency);
            }
            else
            {
                checkForLicenses(context, dependency);
            }
        }
        return dependencies;
    }

    public Set<License> getUsedLicenses(Set<Dependency> dependencies, InputProject project)
    {
        Set<License> usedLicenseList = new TreeSet<>();
        List<License> licenses = licenseService.getLicenses(project);

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

    private void checkForLicenses(SensorContext context, Dependency dependency)
    {
        List<License> licenses = licenseService.getLicenses(context.project());
        if (!checkSpdxLicense(dependency.getLicense(), licenses))
        {
            List<License> licensesContainingDependency = licenses.stream()
                .filter(l -> dependency.getLicense().contains(l.getIdentifier()))
                .collect(Collectors.toList());

            String[] andLicenses = dependency.getLicense().replace("(", "").replace(")", "").split(" AND ");

            if (licensesContainingDependency.size() != andLicenses.length)
            {
                licenseNotFoundIssue(context, dependency);
            }
            else
            {
                StringBuilder notAllowedLicense = new StringBuilder();

                for (License element : licensesContainingDependency)
                {
                    if (!element.getAllowed())
                    {
                        notAllowedLicense.append(element.getName()).append(" ");
                    }
                }
                licenseNotAllowedIssue(context, dependency, notAllowedLicense.toString());
            }
        }
    }

    private boolean checkSpdxLicense(String spdxLicenseString, List<License> licenses)
    {
        if (spdxLicenseString.contains(" OR "))
        {
            return checkSpdxLicenseWithOr(spdxLicenseString, licenses);
        }

        else if (spdxLicenseString.contains(" AND "))
        {
            return checkSpdxLicenseWithAnd(spdxLicenseString, licenses);
        }

        return licenses
            .stream()
            .filter(l -> l.getIdentifier().equals(spdxLicenseString))
            .anyMatch(License::getAllowed);
    }

    private boolean checkSpdxLicenseWithOr(String spdxLicenseString, List<License> licenses)
    {
        String[] orLicenses = spdxLicenseString.replace("(", "").replace(")", "").split(" OR ");
        return licenses
            .stream()
            .filter(l -> ValidateLicenses.contains(orLicenses, l.getIdentifier()))
            .anyMatch(License::getAllowed);
    }

    private boolean checkSpdxLicenseWithAnd(String spdxLicenseString, List<License> licenses)
    {
        String[] andLicenses = spdxLicenseString.replace("(", "").replace(")", "").split(" AND ");
        long count = andLicenses.length;
        List<License> foundLicenses =
            licenses.stream()
                .filter(l -> ValidateLicenses.contains(andLicenses, l.getIdentifier()))
                .collect(Collectors.toList());
        long allowedLicenseCount = foundLicenses.stream().filter(License::getAllowed).count();
        if (count == allowedLicenseCount)
        {
            return true;
        }
        else if (foundLicenses.size() == count)
        {
            // NOT ALLOWED
            return false;
        }
        else
        {
            // NOT FOUND
            return false;
        }
    }

    private void licenseNotAllowedIssue(SensorContext context, Dependency dependency, String notAllowedLicense)
    {
        LOGGER.info("Dependency " + dependency.getName() + " uses a not allowed license " + notAllowedLicense);

        dependency.setStatus(Dependency.Status.Forbidden);

        NewIssue issue = context
            .newIssue()
            .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
        issue.at(issue.newLocation().on(context.project()).message(
            "Dependency " + dependency.getName() + " uses a not allowed license " + dependency.getLicense()));
        issue.save();
    }

    private static void licenseNotFoundIssue(SensorContext context, Dependency dependency)
    {
        LOGGER.info("No License found for Dependency " + dependency.getName());

        dependency.setStatus(Dependency.Status.Unknown);

        NewIssue issue = context
            .newIssue()
            .forRule(RuleKey.of(LicenseCheckMetrics.LICENSE_CHECK_KEY,
                LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
        issue.at(issue.newLocation().on(context.project())
            .message("No License found for Dependency: " + dependency.getName()));
        issue.save();
    }

    private static boolean contains(String[] items, String valueToFind)
    {
        for (String item : items)
        {
            if (item != null && item.equals(valueToFind))
            {
                return true;
            }
        }
        return false;
    }
}
