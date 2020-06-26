package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import static at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration.PARAM_IDENTIFIER;
import static at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration.PARAM_NAME;
import static at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration.PARAM_STATUS;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;

class LicenseAddAction implements RequestHandler
{
    private LicenseSettingsService licenseSettingsService;
    private static final Logger LOGGER = Loggers.get(LicenseAddAction.class);

    public LicenseAddAction(LicenseSettingsService licenseSettingsService)
    {
        this.licenseSettingsService = licenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        License newLicense = new License(
            request.mandatoryParam(PARAM_NAME),
            request.mandatoryParam(PARAM_IDENTIFIER),
            request.mandatoryParam(PARAM_STATUS)
        );

        if (licenseSettingsService.addLicense(newLicense))
        {
            LOGGER.info(LicenseConfiguration.INFO_ADD_SUCCESS + newLicense);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
        }
        else
        {
            LOGGER.error(LicenseConfiguration.ERROR_ADD_ALREADY_EXISTS + newLicense);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
