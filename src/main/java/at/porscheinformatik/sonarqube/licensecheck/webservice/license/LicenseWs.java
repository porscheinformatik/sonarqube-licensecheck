package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import org.sonar.api.server.ws.WebService;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;

public class LicenseWs implements WebService
{

    private final LicenseService licenseService;
    private LicenseSettingsService licenseSettingsService;

    public LicenseWs(LicenseService licenseService, LicenseSettingsService licenseSettingsService)
    {
        this.licenseService = licenseService;
        this.licenseSettingsService = licenseSettingsService;
    }

    @Override
    public void define(Context context)
    {
        NewController controller = context.createController(LicenseConfiguration.CONTROLLER);
        controller
            .createAction(LicenseConfiguration.SHOW_ACTION)
            .setDescription(LicenseConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new LicenseShowAction(licenseService));
        controller
            .createAction(LicenseConfiguration.DELETE_ACTION)
            .setDescription(LicenseConfiguration.DELETE_ACTION_DESCRIPTION)
            .setHandler(new LicenseDeleteAction(licenseSettingsService))
            .createParam(LicenseConfiguration.PARAM);
        controller
            .createAction(LicenseConfiguration.ADD_ACTION)
            .setDescription(LicenseConfiguration.ADD_ACTION_DESCRIPTION)
            .setHandler(new LicenseAddAction(licenseSettingsService))
            .createParam(LicenseConfiguration.PARAM);
        controller
            .createAction(LicenseConfiguration.EDIT_ACTION)
            .setDescription(LicenseConfiguration.EDIT_ACTION_DESCRIPTION)
            .setHandler(new LicenseEditAction(licenseSettingsService))
            .createParam(LicenseConfiguration.PARAM);
        controller.done();
    }
}