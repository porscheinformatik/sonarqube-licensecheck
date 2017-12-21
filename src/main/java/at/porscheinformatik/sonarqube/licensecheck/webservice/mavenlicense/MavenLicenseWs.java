package at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense;

import org.sonar.api.server.ws.WebService;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;

public class MavenLicenseWs implements WebService
{
    private final MavenLicenseService mavenLicenseService;
    private MavenLicenseSettingsService mavenLicenseSettingsService;

    public MavenLicenseWs(MavenLicenseService mavenLicenseService,
        MavenLicenseSettingsService mavenLicenseSettingsService)
    {
        super();
        this.mavenLicenseService = mavenLicenseService;
        this.mavenLicenseSettingsService = mavenLicenseSettingsService;
    }

    @Override
    public void define(Context context)
    {
        NewController controller = context.createController(MavenLicenseConfiguration.CONTROLLER);
        controller
            .createAction(MavenLicenseConfiguration.SHOW_ACTION)
            .setDescription(MavenLicenseConfiguration.SHOW_ACTION_DESCRIPTION)
            .setHandler(new MavenLicenseShowAction(mavenLicenseService));

        NewAction deletAction = controller
            .createAction(MavenLicenseConfiguration.DELETE_ACTION)
            .setDescription(MavenLicenseConfiguration.DELETE_ACTION_DESCRIPTION)
            .setHandler(new MavenLicenseDeleteAction(mavenLicenseSettingsService));
        deletAction.createParam(MavenLicenseConfiguration.PARAM_REGEX).setRequired(true);
            
        NewAction addAction = controller
            .createAction(MavenLicenseConfiguration.ADD_ACTION)
            .setDescription(MavenLicenseConfiguration.ADD_ACTION_DESCRIPTION)
            .setHandler(new MavenLicenseAddAction(mavenLicenseSettingsService));
        addAction.createParam(MavenLicenseConfiguration.PARAM_REGEX).setRequired(true);
        addAction.createParam(MavenLicenseConfiguration.PARAM_LICENSE).setRequired(true);

        NewAction editAction = controller
            .createAction(MavenLicenseConfiguration.EDIT_ACTION)
            .setDescription(MavenLicenseConfiguration.EDIT_ACTION_DESCRIPTION)
            .setHandler(new MavenLicenseEditAction(mavenLicenseSettingsService));
        editAction.createParam(MavenLicenseConfiguration.PARAM_REGEX).setRequired(true);
        editAction.createParam(MavenLicenseConfiguration.PARAM_OLD_REGEX).setRequired(true);
        editAction.createParam(MavenLicenseConfiguration.PARAM_LICENSE).setRequired(true);
        
        controller.done();
    }
}
