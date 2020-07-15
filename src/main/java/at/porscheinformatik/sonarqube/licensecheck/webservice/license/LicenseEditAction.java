package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import static at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration.PARAM_IDENTIFIER;
import static at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration.PARAM_NAME;
import static at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration.PARAM_STATUS;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;

class LicenseEditAction implements RequestHandler
{
    private LicenseSettingsService licenseSettingsService;
    private static final Logger LOGGER = Loggers.get(LicenseEditAction.class);

    public LicenseEditAction(LicenseSettingsService licenseSettingsService)
    {
        this.licenseSettingsService = licenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String id = request.mandatoryParam(PARAM_IDENTIFIER);
        String newName = request.mandatoryParam(PARAM_NAME);
        String newStatus = request.mandatoryParam(PARAM_STATUS);

        licenseSettingsService.updateLicense(id, newName, newStatus);
        LOGGER.info(LicenseConfiguration.INFO_EDIT_SUCCESS, id, newName, newStatus);
        response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
    }
}
