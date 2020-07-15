package at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;

class MavenLicenseAddAction implements RequestHandler
{
    private MavenLicenseSettingsService mavenLicenseSettingsService;
    private static final Logger LOGGER = Loggers.get(MavenLicenseAddAction.class);

    public MavenLicenseAddAction(MavenLicenseSettingsService mavenLicenseSettingsService)
    {
        this.mavenLicenseSettingsService = mavenLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String regex = request.param(MavenLicenseConfiguration.PARAM_REGEX);
        String license = request.param(MavenLicenseConfiguration.PARAM_LICENSE);

        boolean success = mavenLicenseSettingsService.addMavenLicense(regex, license);

        if (success)
        {
            LOGGER.info(MavenLicenseConfiguration.INFO_ADD_SUCCESS + regex + "/" + license);
            mavenLicenseSettingsService.sortMavenLicenses();
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
        }
        else
        {
            LOGGER.error(MavenLicenseConfiguration.ERROR_ADD_ALREADY_EXISTS + regex + "/" + license);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
