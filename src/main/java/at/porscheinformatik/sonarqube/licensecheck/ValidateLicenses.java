package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping;
import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMappingService;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import org.codehaus.plexus.util.StringUtils;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@ScannerSide
public class ValidateLicenses
{
    private static final Logger LOGGER = Loggers.get(ValidateLicenses.class);

    private final LicenseService licenseService;
    private final DependencyMappingService dependencyMappingService;

    public ValidateLicenses(LicenseService licenseService, DependencyMappingService dependencyMappingService)
    {
        super();
        this.licenseService = licenseService;
        this.dependencyMappingService = dependencyMappingService;
    }

    public Set<Dependency> validateLicenses(Set<Dependency> dependencies, SensorContext context)
    {
        List<License> licenses = licenseService.getLicenses(context.project());
        List<DependencyMapping> dependencyMappings = dependencyMappingService.getDependencyMappings();

        for (Dependency dependency : dependencies)
        {
            License license = findLicense(licenses, dependency);
            if (license == null)
            {
                mapDependencyToLicense(dependencyMappings, dependency);
            }

            overrideLicenseForDependency(dependencyMappings, dependency);

            if (!isLicensesValid(context, licenses, dependency))
            {
                dependency.setStatus(Dependency.Status.Unknown);
            }
            else
            {
                dependency.setStatus(Dependency.Status.Allowed);
            }
        }
        return dependencies;
    }

    public Set<License> getUsedLicenses(Set<Dependency> dependencies, InputProject project)
    {
        List<License> licenses = licenseService.getLicenses(project);
        Set<License> usedLicenseList = new TreeSet<>();

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

    private License findLicense(List<License> licenses, Dependency dependency)
    {
        for (License license : licenses)
        {
            if (license.getIdentifier().equals(dependency.getLicense()))
            {
                return license;
            }
        }
        return null;
    }

    private static void mapDependencyToLicense(List<DependencyMapping> dependencyMappings, Dependency dependency)
    {
        dependencyMappings.forEach(dependencyMapping ->
        {
            if (StringUtils.isBlank(dependency.getLicense()))
            {
                String matchString = dependencyMapping.getKey();
                if (dependency.getName().matches(matchString))
                {
                    dependency.setLicense(dependencyMapping.getLicense());
                }
            }
        });
    }

    private static void overrideLicenseForDependency(List<DependencyMapping> dependencyMappings, Dependency dependency)
    {
        dependencyMappings.stream().filter(DependencyMapping::getOverwrite).forEach(dependencyMapping -> {
            String matchString = dependencyMapping.getKey();
            if (dependency.getName().matches(matchString))
            {
                dependency.setLicense(dependencyMapping.getLicense());
            }
        });
    }

    private static boolean isLicensesValid(SensorContext context, List<License> licenses, Dependency dependency)
    {
        if (StringUtils.isBlank(dependency.getLicense()))
        {
            licenseNotFoundIssue(context, dependency);
            return false;
        }

        if (checkSpdxLicense(dependency.getLicense(), licenses))
        {
            return true;
        }

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

        return false;
    }

    private static boolean checkSpdxLicense(String spdxLicenseString, List<License> licenses)
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

    private static boolean checkSpdxLicenseWithOr(String spdxLicenseString, List<License> licenses)
    {
        String[] orLicenses = spdxLicenseString.replace("(", "").replace(")", "").split(" OR ");
        return licenses
            .stream()
            .filter(l -> ValidateLicenses.contains(orLicenses, l.getIdentifier()))
            .anyMatch(License::getAllowed);
    }

    private static boolean checkSpdxLicenseWithAnd(String spdxLicenseString, List<License> licenses)
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

    private static void licenseNotAllowedIssue(SensorContext context, Dependency dependency, String notAllowedLicense)
    {
        LOGGER.info("Dependency " + dependency.getName() + " uses a not allowed license " + notAllowedLicense);

        dependency.setStatus(Dependency.Status.Forbidden);

        createIssue(context, dependency, LicenseCheckRulesDefinition.RULE_NOT_ALLOWED_LICENSE_KEY,
            "Dependency " + dependency.getName() + " uses a not allowed license " + dependency.getLicense());
    }

    private static void licenseNotFoundIssue(SensorContext context, Dependency dependency)
    {
        LOGGER.info("No License found for Dependency " + dependency.getName());

        createIssue(context, dependency, LicenseCheckRulesDefinition.RULE_UNLISTED_KEY,
            "No License found for Dependency: " + dependency.getName());
    }

    private static void createIssue(SensorContext context, Dependency dependency, String rule, String message)
    {
        NewIssue issue = context
            .newIssue()
            .forRule(RuleKey.of(getRepoKey(dependency), rule));

        NewIssueLocation location = issue.newLocation().on(dependency.getInputComponent());
        if (dependency.getTextRange() != null)
        {
            location = location.at(dependency.getTextRange());
        }

        issue.at(location.message(message)).save();
    }

    private static String getRepoKey(Dependency dependency)
    {
        switch (dependency.getLang())
        {
            case LicenseCheckRulesDefinition.LANG_JS:
                return LicenseCheckRulesDefinition.RULE_REPO_KEY_JS;
            case LicenseCheckRulesDefinition.LANG_TS:
                return LicenseCheckRulesDefinition.RULE_REPO_KEY_TS;
            case LicenseCheckRulesDefinition.LANG_GROOVY:
                return LicenseCheckRulesDefinition.RULE_REPO_KEY_GROOVY;
            case LicenseCheckRulesDefinition.LANG_KOTLIN:
                return LicenseCheckRulesDefinition.RULE_REPO_KEY_KOTLIN;
            case LicenseCheckRulesDefinition.LANG_JAVA:
            default:
                return LicenseCheckRulesDefinition.RULE_REPO_KEY;
        }
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
