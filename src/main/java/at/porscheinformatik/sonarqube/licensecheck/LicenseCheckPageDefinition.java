package at.porscheinformatik.sonarqube.licensecheck;

import org.sonar.api.web.page.Context;
import org.sonar.api.web.page.Page;
import org.sonar.api.web.page.PageDefinition;

public class LicenseCheckPageDefinition implements PageDefinition {

    @Override
    public void define(Context context) {
        context.addPage(
            Page.builder("licensecheck/configuration")
                .setName("License Check")
                .setAdmin(true)
                .build()
        );

        context.addPage(
            Page.builder("licensecheck/dashboard")
                .setName("License Check")
                .setScope(Page.Scope.COMPONENT)
                .setComponentQualifiers(Page.Qualifier.PROJECT)
                .build()
        );
    }
}
