package at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;

class MavenLicenseDeleteAction implements RequestHandler
{
    private MavenLicenseSettingsService mavenLicenseSettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenLicenseDeleteAction.class);

    public MavenLicenseDeleteAction(MavenLicenseSettingsService mavenLicenseSettingsService)
    {
        this.mavenLicenseSettingsService = mavenLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(MavenLicenseConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        if (StringUtils.isNotBlank(jsonObject.getString(MavenLicenseConfiguration.PROPERTY_REGEX)))
        {
            mavenLicenseSettingsService
                .deleteMavenLicense(jsonObject.getString(MavenLicenseConfiguration.PROPERTY_REGEX));
            LOGGER.info(MavenLicenseConfiguration.INFO_DELETE_SUCCESS + jsonObject.toString());
            mavenLicenseSettingsService.sortMavenLicenses();
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
        }
        else
        {
            LOGGER.error(MavenLicenseConfiguration.ERROR_DELETE_INVALID_INPUT + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
