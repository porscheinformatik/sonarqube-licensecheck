package at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenDependencyConfiguration;

class MavenDependencyAddAction implements RequestHandler
{
    private final MavenDependencySettingsService mavenDependencySettingsService;
    private static final Logger LOGGER = Loggers.get(MavenDependencyAddAction.class);

    public MavenDependencyAddAction(MavenDependencySettingsService mavenDependencySettingsService)
    {
        this.mavenDependencySettingsService = mavenDependencySettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String key = request.param(MavenDependencyConfiguration.PARAM_KEY);
        String license = request.param(MavenDependencyConfiguration.PARAM_LICENSE);

        boolean success = mavenDependencySettingsService.addMavenDependency(key, license);

        if (success)
        {
            LOGGER.info(MavenDependencyConfiguration.INFO_ADD_SUCCESS + key + "/" + license);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
        }
        else
        {
            LOGGER.error(MavenDependencyConfiguration.ERROR_ADD_ALREADY_EXISTS + key + "/" + license);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
