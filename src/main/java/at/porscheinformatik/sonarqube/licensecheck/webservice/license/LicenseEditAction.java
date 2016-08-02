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

import at.porscheinformatik.sonarqube.licensecheck.license.License;
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
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(LicenseConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        boolean newIdentifierIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(LicenseConfiguration.PROPERTY_NEW_IDENTIFIER));
        boolean newNameIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(LicenseConfiguration.PROPERTY_NEW_NAME));
        boolean newStatusIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(LicenseConfiguration.PROPERTY_NEW_STATUS));
        boolean oldIdentifierIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(LicenseConfiguration.PROPERTY_OLD_IDENTIFIER));

        if (newIdentifierIsNotBlank && newNameIsNotBlank && newStatusIsNotBlank && oldIdentifierIsNotBlank)
        {
            License newLicense = new License(jsonObject.getString(LicenseConfiguration.PROPERTY_NEW_NAME),
                jsonObject.getString(LicenseConfiguration.PROPERTY_NEW_IDENTIFIER),
                jsonObject.getString(LicenseConfiguration.PROPERTY_NEW_STATUS));

            if (!licenseSettingsService.checkIfListContains(newLicense))
            {
                licenseSettingsService
                    .deleteLicense(jsonObject.getString(LicenseConfiguration.PROPERTY_OLD_IDENTIFIER));
                licenseSettingsService.addLicense(newLicense);
                licenseSettingsService.sortLicenses();
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
                LOGGER.info(LicenseConfiguration.INFO_EDIT_SUCCESS + jsonObject.toString());
            }
            else
            {
                LOGGER.error(LicenseConfiguration.ERROR_EDIT_ALREADY_EXISTS + jsonObject.toString());
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
            }

        }
        else
        {
            LOGGER.error(LicenseConfiguration.ERROR_EDIT_INVALID_INPUT + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
