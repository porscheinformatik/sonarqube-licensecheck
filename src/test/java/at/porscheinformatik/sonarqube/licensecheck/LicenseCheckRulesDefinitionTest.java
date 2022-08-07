package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;

public class LicenseCheckRulesDefinitionTest
{
    @Test
    public void define()
    {
        RulesDefinition.Context context = new RulesDefinition.Context();

        new LicenseCheckRulesDefinition().define(context);

        assertThat(context.repositories().size(), is(6));
        for (RulesDefinition.Repository repository : context.repositories())
        {
            assertThat(repository.rules().size(), is(2));
        }
    }
}
