package at.porscheinformatik.sonarqube.licensecheck.widget;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.Description;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.UserRole;

@UserRole(UserRole.USER)
@Description("Shows all licenses, which are used by the dependencies")
public class UsedLicensesWidget extends AbstractRubyTemplate implements RubyRailsWidget
{
    @Override
    public String getId()
    {
        return "usedLicenses";
    }

    @Override
    public String getTitle()
    {
        return "Used Licenses";
    }

    @Override
    protected String getTemplatePath()
    {
        return "/at/porscheinformatik/sonarqube/licensecheck/widget/usedlicenses_widget.html.erb";
    }
}
