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
        controller
            .createAction(MavenDependencyConfiguration.DELETE_ACTION)
            .setDescription(MavenDependencyConfiguration.DELETE_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyDeleteAction(mavenDependencySettingsService))
            .createParam(MavenDependencyConfiguration.PARAM);
        controller
            .createAction(MavenDependencyConfiguration.ADD_ACTION)
            .setDescription(MavenDependencyConfiguration.ADD_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyAddAction(mavenDependencySettingsService))
            .createParam(MavenDependencyConfiguration.PARAM);
        controller
            .createAction(MavenDependencyConfiguration.EDIT_ACTION)
            .setDescription(MavenDependencyConfiguration.EDIT_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyEditAction(mavenDependencySettingsService))
            .createParam(MavenDependencyConfiguration.PARAM);
        controller.done();
    }
}