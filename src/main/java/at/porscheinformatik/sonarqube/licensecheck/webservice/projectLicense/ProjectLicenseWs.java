package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.ProjectLicenseConfiguration;
import org.sonar.api.server.ws.WebService;

public class ProjectLicenseWs implements WebService
{
    private final ProjectLicenseService projectLicenseService;

    public ProjectLicenseWs(ProjectLicenseService projectLicenseService)
    {
        this.projectLicenseService = projectLicenseService;
    }

    @Override
    public void define(Context context)
    {
        NewController controller = context.createController(ProjectLicenseConfiguration.CONTROLLER);

        controller
            .createAction(ProjectLicenseConfiguration.SHOW_ACTION)
            .setDescription(ProjectLicenseConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new ProjectLicenseShowAction(projectLicenseService));

        controller.done();
    }
}
