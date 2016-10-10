package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;

class LicenseDeleteAction implements RequestHandler
{
    private LicenseSettingsService licenseSettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseDeleteAction.class);

    public LicenseDeleteAction(LicenseSettingsService licenseSettingsService)
    {
        this.licenseSettingsService = licenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String licenseId = request.param(LicenseConfiguration.PARAM_IDENTIFIER);
        LOGGER.warn("Deleting license: " + licenseId);
        if (licenseSettingsService.deleteLicense(licenseId))
        {
            LOGGER.info(LicenseConfiguration.INFO_DELETE_SUCCESS, licenseId);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
        }
        else
        {
            LOGGER.error(LicenseConfiguration.ERROR_DELETE_LICENSE, licenseId);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_FOUND);
        }
    }
}
