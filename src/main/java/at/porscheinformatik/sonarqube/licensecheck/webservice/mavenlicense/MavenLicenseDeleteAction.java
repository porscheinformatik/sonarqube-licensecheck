package at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;

class MavenLicenseDeleteAction implements RequestHandler
{
    private MavenLicenseSettingsService mavenLicenseSettingsService;
    private static final Logger LOGGER = Loggers.get(MavenLicenseDeleteAction.class);

    public MavenLicenseDeleteAction(MavenLicenseSettingsService mavenLicenseSettingsService)
    {
        this.mavenLicenseSettingsService = mavenLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String licenseNameRegex = request.param(MavenLicenseConfiguration.PARAM_REGEX);
        mavenLicenseSettingsService.deleteMavenLicense(licenseNameRegex);
        LOGGER.info(MavenLicenseConfiguration.INFO_DELETE_SUCCESS + licenseNameRegex);
        mavenLicenseSettingsService.sortMavenLicenses();
        response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
    }
}
