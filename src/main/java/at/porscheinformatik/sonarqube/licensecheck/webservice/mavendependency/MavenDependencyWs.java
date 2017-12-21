package at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency;

import org.sonar.api.server.ws.WebService;

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenDependencyConfiguration;

public class MavenDependencyWs implements WebService
{

    private final MavenDependencyService mavenDependencyService;
    private MavenDependencySettingsService mavenDependencySettingsService;

    public MavenDependencyWs(MavenDependencyService mavenDependencyService,
        MavenDependencySettingsService dependencySettingsService)
    {
        this.mavenDependencyService = mavenDependencyService;
        this.mavenDependencySettingsService = dependencySettingsService;
    }

    @Override
    public void define(Context context)
    {
        NewController controller = context.createController(MavenDependencyConfiguration.CONTROLLER);
        controller
            .createAction(MavenDependencyConfiguration.SHOW_ACTION)
            .setDescription(MavenDependencyConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyShowAction(mavenDependencyService));

        NewAction deleteAction = controller
            .createAction(MavenDependencyConfiguration.DELETE_ACTION)
            .setDescription(MavenDependencyConfiguration.DELETE_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyDeleteAction(mavenDependencySettingsService));
        deleteAction.createParam(MavenDependencyConfiguration.PARAM_KEY).setRequired(true);

        NewAction addAction = controller
            .createAction(MavenDependencyConfiguration.ADD_ACTION)
            .setDescription(MavenDependencyConfiguration.ADD_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyAddAction(mavenDependencySettingsService));
        addAction.createParam(MavenDependencyConfiguration.PARAM_KEY).setRequired(true);
        addAction.createParam(MavenDependencyConfiguration.PARAM_LICENSE).setRequired(true);

        NewAction editAction = controller
            .createAction(MavenDependencyConfiguration.EDIT_ACTION)
            .setDescription(MavenDependencyConfiguration.EDIT_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyEditAction(mavenDependencySettingsService));
        editAction.createParam(MavenDependencyConfiguration.PARAM_KEY).setRequired(true);
        editAction.createParam(MavenDependencyConfiguration.PARAM_OLD_KEY).setRequired(true);
        editAction.createParam(MavenDependencyConfiguration.PARAM_LICENSE).setRequired(true);

        controller.done();
    }
}
