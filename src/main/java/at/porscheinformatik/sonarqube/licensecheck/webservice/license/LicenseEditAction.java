package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import static at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;

class LicenseEditAction implements RequestHandler
{
    private LicenseSettingsService licenseSettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseEditAction.class);

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
