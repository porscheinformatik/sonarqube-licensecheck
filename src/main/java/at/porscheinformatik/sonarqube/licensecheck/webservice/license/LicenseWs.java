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
            .createParam(LicenseConfiguration.PARAM_IDENTIFIER).setRequired(true);

        NewAction addAction = controller
            .createAction(LicenseConfiguration.ADD_ACTION)
            .setDescription(LicenseConfiguration.ADD_ACTION_DESCRIPTION)
            .setHandler(new LicenseAddAction(licenseSettingsService));
        addAction.createParam(LicenseConfiguration.PARAM_IDENTIFIER).setRequired(true);
        addAction.createParam(LicenseConfiguration.PARAM_STATUS).setPossibleValues("true", "false").setRequired(true);
        addAction.createParam(LicenseConfiguration.PARAM_NAME).setRequired(true);

        NewAction editAction = controller
            .createAction(LicenseConfiguration.EDIT_ACTION)
            .setDescription(LicenseConfiguration.EDIT_ACTION_DESCRIPTION)
            .setHandler(new LicenseEditAction(licenseSettingsService));
        editAction.createParam(LicenseConfiguration.PARAM_IDENTIFIER).setRequired(true);
        editAction.createParam(LicenseConfiguration.PARAM_STATUS).setPossibleValues("true", "false").setRequired(true);
        editAction.createParam(LicenseConfiguration.PARAM_NAME).setRequired(true);

        controller.done();
    }
}
