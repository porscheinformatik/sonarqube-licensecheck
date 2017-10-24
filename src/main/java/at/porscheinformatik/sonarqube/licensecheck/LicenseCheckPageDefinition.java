package at.porscheinformatik.sonarqube.licensecheck;

import org.sonar.api.web.page.Context;
import org.sonar.api.web.page.Page;
import org.sonar.api.web.page.PageDefinition;

public class LicenseCheckPageDefinition implements PageDefinition
{
    @Override
    public void define(Context context)
    {
        context
            .addPage(Page.builder("licensecheck/configuration").setName("License Check").setAdmin(true).build());
    }
}
