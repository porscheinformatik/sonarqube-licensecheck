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

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicense;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;

class MavenLicenseEditAction implements RequestHandler
{
    private MavenLicenseSettingsService mavenLicenseSettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenLicenseEditAction.class);

    public MavenLicenseEditAction(MavenLicenseSettingsService mavenLicenseSettingsService)
    {
        this.mavenLicenseSettingsService = mavenLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(MavenLicenseConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        boolean oldRegexIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(MavenLicenseConfiguration.PROPERTY_OLD_REGEX));
        boolean newRegexIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(MavenLicenseConfiguration.PROPERTY_NEW_REGEX));
        boolean newKeyIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(MavenLicenseConfiguration.PROPERTY_NEW_KEY));

        if (oldRegexIsNotBlank && newRegexIsNotBlank && newKeyIsNotBlank)
        {
            MavenLicense newMavenLicense =
                new MavenLicense(jsonObject.getString(MavenLicenseConfiguration.PROPERTY_NEW_REGEX),
                    jsonObject.getString(MavenLicenseConfiguration.PROPERTY_NEW_KEY));

            if (!mavenLicenseSettingsService.checkIfListContains(newMavenLicense))
            {
                mavenLicenseSettingsService
                    .deleteMavenLicense(jsonObject.getString(MavenLicenseConfiguration.PROPERTY_OLD_REGEX));
                mavenLicenseSettingsService.addMavenLicense(newMavenLicense);
                mavenLicenseSettingsService.sortMavenLicenses();
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
                LOGGER.info(MavenLicenseConfiguration.INFO_EDIT_SUCCESS + jsonObject.toString());
            }
            else
            {
                LOGGER.error(MavenLicenseConfiguration.ERROR_EDIT_ALREADY_EXISTS + jsonObject.toString());
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
            }

        }
        else
        {
            LOGGER.error(MavenLicenseConfiguration.ERROR_EDIT_INVALID_INPUT + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
