package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

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

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;

class LicenseAddAction implements RequestHandler
{
    private LicenseSettingsService licenseSettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseAddAction.class);

    public LicenseAddAction(LicenseSettingsService licenseSettingsService)
    {
        this.licenseSettingsService = licenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(LicenseConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        boolean identifierIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(LicenseConfiguration.PROPERTY_IDENTIFIER));
        boolean nameIsNotBlank = StringUtils.isNotBlank(jsonObject.getString(LicenseConfiguration.PROPERTY_NAME));
        boolean statusIsNotBlank = StringUtils.isNotBlank(jsonObject.getString(LicenseConfiguration.PROPERTY_STATUS));

        if (identifierIsNotBlank && nameIsNotBlank && statusIsNotBlank)
        {
            boolean success =
                licenseSettingsService.addLicense(jsonObject.getString(LicenseConfiguration.PROPERTY_NAME),
                    jsonObject.getString(LicenseConfiguration.PROPERTY_IDENTIFIER),
                    jsonObject.getString(LicenseConfiguration.PROPERTY_STATUS));

            if (success)
            {
                LOGGER.info(LicenseConfiguration.INFO_ADD_SUCCESS + jsonObject.toString());
                licenseSettingsService.sortLicenses();
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
            }
            else
            {
                LOGGER.error(LicenseConfiguration.ERROR_ADD_ALREADY_EXISTS + jsonObject.toString());
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
            }
        }
        else
        {
            LOGGER.error(LicenseConfiguration.ERROR_ADD_INVALID_INPUT + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
