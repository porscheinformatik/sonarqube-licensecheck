package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

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

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.ProjectLicenseConfiguration;

class ProjectLicenseAddAction implements RequestHandler
{
    private ProjectLicenseSettingsService projectLicenseSettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectLicenseAddAction.class);

    public ProjectLicenseAddAction(ProjectLicenseSettingsService projectLicenseSettingsService)
    {
        this.projectLicenseSettingsService = projectLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(ProjectLicenseConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        boolean licenseIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_LICENSE));
        boolean projectNameIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_PROJECT_NAME));
        boolean statusIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_STATUS));
        boolean projectKeyIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_PROJECT_KEY));

        if (licenseIsNotBlank && projectNameIsNotBlank && statusIsNotBlank && projectKeyIsNotBlank)
        {
            boolean success = projectLicenseSettingsService.addProjectLicense(
                jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_LICENSE),
                jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_PROJECT_NAME),
                jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_STATUS),
                jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_PROJECT_KEY));

            if (success)
            {
                LOGGER.info(ProjectLicenseConfiguration.INFO_ADD_SUCCESS + jsonObject.toString());
                projectLicenseSettingsService.sortProjectLicenses();
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
            }
            else
            {
                LOGGER.error(ProjectLicenseConfiguration.ERROR_ADD_ALREADY_EXISTS + jsonObject.toString());
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
            }
        }
        else
        {
            LOGGER.error(ProjectLicenseConfiguration.ERROR_ADD_INVALID_INPUT + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
