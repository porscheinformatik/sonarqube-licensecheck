package at.porscheinformatik.sonarqube.licensecheck.widget;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.Description;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.UserRole;

@UserRole(UserRole.USER)
@Description("Shows json and maven dependencies")
public class DependencyCheckWidget extends AbstractRubyTemplate implements RubyRailsWidget
{
    @Override
    public String getId()
    {
        return "licenseData";
    }

    @Override
    public String getTitle()
    {
        return "License Data";
    }

    @Override
    protected String getTemplatePath()
    {
        return "/at/porscheinformatik/sonarqube/licensecheck/widget/dependencycheck_widget.html.erb";
    }
}
