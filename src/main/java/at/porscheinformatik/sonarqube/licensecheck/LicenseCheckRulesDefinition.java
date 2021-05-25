package at.porscheinformatik.sonarqube.licensecheck;

import org.sonar.api.rule.Severity;
import org.sonar.api.server.rule.RulesDefinition;

/**
 * Repository for the rules used in the plugin
 */
public final class LicenseCheckRulesDefinition implements RulesDefinition
{
    public static final String LANG_JAVA = "java";
    public static final String LANG_JS = "js";
    public static final String LANG_GROOVY = "grvy";

    public static final String RULE_REPO_KEY = "licensecheck";
    public static final String RULE_REPO_KEY_JS = "licensecheck-js";
    public static final String RULE_REPO_KEY_GROOVY = "licensecheck-groovy";

    public static final String RULE_UNLISTED_KEY = "licensecheck.unlisted";
    public static final String RULE_NOT_ALLOWED_LICENSE_KEY = "licensecheck.notallowedlicense";

    @Override
    public void define(Context context)
    {
        NewRepository[] repos = new NewRepository[]{
            context.createRepository(RULE_REPO_KEY, LANG_JAVA),
            context.createRepository(RULE_REPO_KEY_JS, LANG_JS),
            context.createRepository(RULE_REPO_KEY_GROOVY, LANG_GROOVY)
        };

        for (NewRepository repo : repos)
        {
            repo.setName("License Check");

            repo
                .createRule(RULE_UNLISTED_KEY)
                .setName("Dependency has unknown license [license-check]")
                .setHtmlDescription("The dependencies license could not be determined!")
                .setSeverity(Severity.BLOCKER);

            repo
                .createRule(RULE_NOT_ALLOWED_LICENSE_KEY)
                .setName("License is not allowed [license-check]")
                .setHtmlDescription("Violation because the license of the dependency is not allowed.")
                .setSeverity(Severity.BLOCKER);

            repo.done();
        }
    }
}
