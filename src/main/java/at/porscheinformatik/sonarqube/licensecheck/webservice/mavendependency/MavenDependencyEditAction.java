package at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency;

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

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenDependencyConfiguration;

class MavenDependencyEditAction implements RequestHandler
{
    private MavenDependencySettingsService mavenDependencySettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyEditAction.class);

    public MavenDependencyEditAction(MavenDependencySettingsService mavenDependencySettingsService)
    {
        this.mavenDependencySettingsService = mavenDependencySettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(MavenDependencyConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        boolean oldKeyIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(MavenDependencyConfiguration.PROPERTY_OLD_KEY));
        boolean newKeyIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(MavenDependencyConfiguration.PROPERTY_NEW_KEY));
        boolean newLicenseIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(MavenDependencyConfiguration.PROPERTY_NEW_LICENSE));

        if (oldKeyIsNotBlank && newKeyIsNotBlank && newLicenseIsNotBlank)
        {
            MavenDependency newMavenDependency =
                new MavenDependency(jsonObject.getString(MavenDependencyConfiguration.PROPERTY_NEW_KEY),
                    jsonObject.getString(MavenDependencyConfiguration.PROPERTY_NEW_LICENSE));

            if (!mavenDependencySettingsService.hasDependency(newMavenDependency))
            {
                mavenDependencySettingsService
                    .deleteMavenDependency(jsonObject.getString(MavenDependencyConfiguration.PROPERTY_OLD_KEY));
                mavenDependencySettingsService.addMavenDependency(newMavenDependency);
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
                LOGGER.info(MavenDependencyConfiguration.INFO_EDIT_SUCCESS + jsonObject.toString());
            }
            else
            {
                LOGGER.error(MavenDependencyConfiguration.ERROR_EDIT_ALREADY_EXISTS + jsonObject.toString());
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
            }

        }
        else
        {
            LOGGER.error(MavenDependencyConfiguration.ERROR_ADD_INVALID_INPUT + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
