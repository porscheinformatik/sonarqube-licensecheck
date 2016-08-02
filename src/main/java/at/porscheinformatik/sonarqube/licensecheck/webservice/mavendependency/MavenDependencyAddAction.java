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

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenDependencyConfiguration;

class MavenDependencyAddAction implements RequestHandler
{
    private MavenDependencySettingsService mavenDependencySettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyAddAction.class);

    public MavenDependencyAddAction(MavenDependencySettingsService mavenDependencySettingsService)
    {
        this.mavenDependencySettingsService = mavenDependencySettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(MavenDependencyConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        boolean keyIsNotBlank = StringUtils.isNotBlank(jsonObject.getString(MavenDependencyConfiguration.PROPERTY_KEY));
        boolean licenseIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(MavenDependencyConfiguration.PROPERTY_LICENSE));

        if (keyIsNotBlank && licenseIsNotBlank)
        {
            boolean success = mavenDependencySettingsService.addMavenDependency(
                jsonObject.getString(MavenDependencyConfiguration.PROPERTY_KEY),
                jsonObject.getString(MavenDependencyConfiguration.PROPERTY_LICENSE));

            if (success)
            {
                LOGGER.info(MavenDependencyConfiguration.INFO_ADD_SUCCESS + jsonObject.toString());
                mavenDependencySettingsService.sortDependencies();
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
            }
            else
            {
                LOGGER.error(MavenDependencyConfiguration.ERROR_ADD_ALREADY_EXISTS + jsonObject.toString());
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
            }
        }
        else
        {
            LOGGER.error(MavenDependencyConfiguration.INFO_ADD_SUCCESS + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
