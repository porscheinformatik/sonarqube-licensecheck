package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

import org.sonar.api.server.ws.WebService;

import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseSettingsService;
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

        NewAction deleteAction = controller
            .createAction(ProjectLicenseConfiguration.DELETE_ACTION)
            .setDescription(ProjectLicenseConfiguration.DELETE_ACTION_DESCRIPTION)
            .setHandler(new ProjectLicenseDeleteAction(projectLicenseSettingsService));
        deleteAction.createParam(ProjectLicenseConfiguration.PARAM_PROJECT_KEY).setRequired(true);
        deleteAction.createParam(ProjectLicenseConfiguration.PARAM_LICENSE).setRequired(true);

        NewAction addAction = controller
            .createAction(ProjectLicenseConfiguration.ADD_ACTION)
            .setDescription(ProjectLicenseConfiguration.ADD_ACTION_DESCRIPTION)
            .setPost(true)
            .setHandler(new ProjectLicenseAddAction(projectLicenseSettingsService));
        addAction.createParam(ProjectLicenseConfiguration.PARAM_PROJECT_KEY).setRequired(true);
        addAction.createParam(ProjectLicenseConfiguration.PARAM_LICENSE).setRequired(true);
        addAction.createParam(ProjectLicenseConfiguration.PARAM_STATUS)
            .setRequired(true)
            .setPossibleValues("true", "false");

        NewAction editAction = controller
            .createAction(ProjectLicenseConfiguration.EDIT_ACTION)
            .setDescription(ProjectLicenseConfiguration.EDIT_ACTION_DESCRIPTION)
            .setPost(true)
            .setHandler(new ProjectLicenseEditAction(projectLicenseSettingsService));

        editAction.createParam(ProjectLicenseConfiguration.PARAM_PROJECT_KEY).setRequired(true);
        editAction.createParam(ProjectLicenseConfiguration.PARAM_LICENSE).setRequired(true);
        editAction.createParam(ProjectLicenseConfiguration.PARAM_STATUS)
            .setRequired(true)
            .setPossibleValues("true", "false");

        controller.done();
    }
}
