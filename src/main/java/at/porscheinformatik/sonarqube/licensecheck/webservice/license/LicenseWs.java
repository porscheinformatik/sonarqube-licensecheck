package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;
import org.sonar.api.server.ws.WebService;

public class LicenseWs implements WebService
{
    private final LicenseService licenseService;

    public LicenseWs(LicenseService licenseService)
    {
        this.licenseService = licenseService;
    }

    @Override
    public void define(Context context)
    {
        //TODO: Also implement the show actions in VUE to remove those controllers here at all.
        NewController controller = context.createController(LicenseConfiguration.CONTROLLER);

        controller
            .createAction(LicenseConfiguration.SHOW_ACTION)
            .setDescription(LicenseConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new LicenseShowAction(licenseService));

        controller.done();
    }
}
