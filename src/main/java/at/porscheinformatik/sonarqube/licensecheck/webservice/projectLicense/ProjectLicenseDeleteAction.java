package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.ProjectLicenseConfiguration;

class ProjectLicenseDeleteAction implements RequestHandler
{
    private ProjectLicenseSettingsService projectLicenseSettingsService;
    private static final Logger LOGGER = Loggers.get(ProjectLicenseDeleteAction.class);

    public ProjectLicenseDeleteAction(ProjectLicenseSettingsService projectLicenseSettingsService)
    {
        this.projectLicenseSettingsService = projectLicenseSettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String projectKey = request.param(ProjectLicenseConfiguration.PARAM_PROJECT_KEY);
        String license = request.param(ProjectLicenseConfiguration.PARAM_LICENSE);

        projectLicenseSettingsService.deleteProjectLicense(projectKey, license);

        LOGGER.info(ProjectLicenseConfiguration.INFO_DELETE_SUCCESS + projectKey + '/' + license);

        response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
    }
}
