package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicense;
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
        ProjectLicense projectLicense = new ProjectLicense(
            request.mandatoryParam(ProjectLicenseConfiguration.PARAM_PROJECT_KEY),
            request.mandatoryParam(ProjectLicenseConfiguration.PARAM_LICENSE),
            request.mandatoryParam(ProjectLicenseConfiguration.PARAM_STATUS)
        );
        boolean success = projectLicenseSettingsService.addProjectLicense(projectLicense);

        if (success)
        {
            LOGGER.info(ProjectLicenseConfiguration.INFO_ADD_SUCCESS + projectLicense);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
        }
        else
        {
            LOGGER.error(ProjectLicenseConfiguration.ERROR_ADD_ALREADY_EXISTS + projectLicense);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
