package at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;
import org.sonar.api.server.ws.WebService;

public class MavenLicenseWs implements WebService
{
    private final MavenLicenseService mavenLicenseService;

    public MavenLicenseWs(MavenLicenseService mavenLicenseService)
    {
        super();
        this.mavenLicenseService = mavenLicenseService;
    }

    @Override
    public void define(Context context)
    {
        NewController controller = context.createController(MavenLicenseConfiguration.CONTROLLER);
        controller
            .createAction(MavenLicenseConfiguration.SHOW_ACTION)
            .setDescription(MavenLicenseConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new MavenLicenseShowAction(mavenLicenseService));

        controller.done();
    }
}
