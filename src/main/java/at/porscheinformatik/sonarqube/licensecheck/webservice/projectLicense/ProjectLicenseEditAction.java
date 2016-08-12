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

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.ProjectLicenseConfiguration;

class ProjectLicenseEditAction implements RequestHandler
{
    private ProjectLicenseSettingsService projectLicenseSettingsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectLicenseEditAction.class);

    public ProjectLicenseEditAction(ProjectLicenseSettingsService projectLicenseSettingsService)
    {
        this.projectLicenseSettingsService = projectLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        JsonReader jsonReader = Json.createReader(new StringReader(request.param(ProjectLicenseConfiguration.PARAM)));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        boolean newLicenseIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_LICENSE));
        boolean newProjectNameIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_PROJECT_NAME));
        boolean newStatusIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_STATUS));
        boolean newProjectKeyIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_PROJECT_KEY));
        boolean oldLicenseIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_LICENSE));
        boolean oldProjectNameIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_PROJECT_NAME));
        boolean oldStatusIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_STATUS));
        boolean oldProjectKeyIsNotBlank =
            StringUtils.isNotBlank(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_PROJECT_KEY));

        if (newLicenseIsNotBlank
            && newProjectNameIsNotBlank
            && newStatusIsNotBlank
            && newProjectKeyIsNotBlank
            && oldLicenseIsNotBlank
            && oldProjectNameIsNotBlank
            && oldStatusIsNotBlank
            && oldProjectKeyIsNotBlank)
        {
            ProjectLicense newProjectLicense =
                new ProjectLicense(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_LICENSE),
                    jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_PROJECT_NAME),
                    jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_STATUS),
                    jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_NEW_PROJECT_KEY));

            ProjectLicense oldProjectLicense =
                new ProjectLicense(jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_LICENSE),
                    jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_PROJECT_NAME),
                    jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_STATUS),
                    jsonObject.getString(ProjectLicenseConfiguration.PROPERTY_OLD_PROJECT_KEY));

            if (!projectLicenseSettingsService.checkIfListContains(newProjectLicense)
                || (oldProjectLicense.equals(newProjectLicense)
                    && !oldProjectLicense.getStatus().equals(newProjectLicense.getStatus())))
            {
                projectLicenseSettingsService.updateProjectLicense(oldProjectLicense, newProjectLicense);
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
                LOGGER.info(ProjectLicenseConfiguration.INFO_EDIT_SUCCESS + jsonObject.toString());
            }
            else
            {
                LOGGER.error(ProjectLicenseConfiguration.ERROR_EDIT_ALREADY_EXISTS + jsonObject.toString());
                response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
            }

        }
        else
        {
            LOGGER.error(ProjectLicenseConfiguration.ERROR_EDIT_INVALID_INPUT + jsonObject.toString());
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
