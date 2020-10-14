package at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency;

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenDependencyConfiguration;
import org.sonar.api.server.ws.WebService;

public class MavenDependencyWs implements WebService
{

    private final MavenDependencyService mavenDependencyService;

    public MavenDependencyWs(MavenDependencyService mavenDependencyService)
    {
        this.mavenDependencyService = mavenDependencyService;
    }

    @Override
    public void define(Context context)
    {
        NewController controller = context.createController(MavenDependencyConfiguration.CONTROLLER);
        controller
            .createAction(MavenDependencyConfiguration.SHOW_ACTION)
            .setDescription(MavenDependencyConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new MavenDependencyShowAction(mavenDependencyService));

        controller.done();
    }
}
