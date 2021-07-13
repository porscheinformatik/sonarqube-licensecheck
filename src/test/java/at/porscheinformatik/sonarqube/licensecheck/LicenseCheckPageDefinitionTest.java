package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.sonar.api.web.page.Context;

public class LicenseCheckPageDefinitionTest
{
    @Test
    public void define()
    {
        LicenseCheckPageDefinition definition = new LicenseCheckPageDefinition();
        Context context = new Context();
        definition.define(context);

        assertThat(context.getPages().size(), is(2));
    }
}
