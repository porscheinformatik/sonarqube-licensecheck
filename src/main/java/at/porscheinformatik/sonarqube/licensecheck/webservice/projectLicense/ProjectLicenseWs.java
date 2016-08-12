package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

import org.sonar.api.server.ws.WebService;

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.ProjectLicenseConfiguration;

public class ProjectLicenseWs implements WebService
{

    private final ProjectLicenseService projectLicenseService;
    private ProjectLicenseSettingsService projectLicenseSettingsService;

    public ProjectLicenseWs(ProjectLicenseService projectLicenseService,
        ProjectLicenseSettingsService projectLicenseSettingsService)
    {
        this.projectLicenseService = projectLicenseService;
        this.projectLicenseSettingsService = projectLicenseSettingsService;
    }

    @Override
    public void define(Context context)
    {
        NewController controller = context.createController(ProjectLicenseConfiguration.CONTROLLER);
        controller
            .createAction(ProjectLicenseConfiguration.SHOW_ACTION)
            .setDescription(ProjectLicenseConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new ProjectLicenseShowAction(projectLicenseService));
        controller
            .createAction(ProjectLicenseConfiguration.DELETE_ACTION)
            .setDescription(ProjectLicenseConfiguration.DELETE_ACTION_DESCRIPTION)
            .setHandler(new ProjectLicenseDeleteAction(projectLicenseSettingsService))
            .createParam(ProjectLicenseConfiguration.PARAM);
        controller
            .createAction(ProjectLicenseConfiguration.ADD_ACTION)
            .setDescription(ProjectLicenseConfiguration.ADD_ACTION_DESCRIPTION)
            .setHandler(new ProjectLicenseAddAction(projectLicenseSettingsService))
            .createParam(ProjectLicenseConfiguration.PARAM);
        controller
            .createAction(ProjectLicenseConfiguration.EDIT_ACTION)
            .setDescription(ProjectLicenseConfiguration.EDIT_ACTION_DESCRIPTION)
            .setHandler(new ProjectLicenseEditAction(projectLicenseSettingsService))
            .createParam(ProjectLicenseConfiguration.PARAM);
        controller.done();
    }
}