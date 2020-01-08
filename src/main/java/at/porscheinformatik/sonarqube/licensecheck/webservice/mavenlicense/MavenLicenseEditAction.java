package at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicense;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;

class MavenLicenseEditAction implements RequestHandler
{
    private MavenLicenseSettingsService mavenLicenseSettingsService;
    private static final Logger LOGGER = Loggers.get(MavenLicenseEditAction.class);

    public MavenLicenseEditAction(MavenLicenseSettingsService mavenLicenseSettingsService)
    {
        this.mavenLicenseSettingsService = mavenLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String regex = request.param(MavenLicenseConfiguration.PARAM_REGEX);
        String license = request.param(MavenLicenseConfiguration.PARAM_LICENSE);
        String oldRegex = request.param(MavenLicenseConfiguration.PARAM_OLD_REGEX);

        MavenLicense newMavenLicense = new MavenLicense(regex, license);

        if (!mavenLicenseSettingsService.checkIfListContains(newMavenLicense))
        {
            mavenLicenseSettingsService.deleteMavenLicense(oldRegex);
            mavenLicenseSettingsService.addMavenLicense(newMavenLicense);
            mavenLicenseSettingsService.sortMavenLicenses();
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
            LOGGER.info(MavenLicenseConfiguration.INFO_EDIT_SUCCESS + newMavenLicense);
        }
        else
        {
            LOGGER.error(MavenLicenseConfiguration.ERROR_EDIT_ALREADY_EXISTS + newMavenLicense);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
