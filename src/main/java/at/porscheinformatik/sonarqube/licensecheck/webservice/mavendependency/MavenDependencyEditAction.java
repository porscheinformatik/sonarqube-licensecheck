package at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.HTTPConfiguration;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenDependencyConfiguration;

class MavenDependencyEditAction implements RequestHandler
{
    private MavenDependencySettingsService mavenDependencySettingsService;
    private static final Logger LOGGER = Loggers.get(MavenDependencyEditAction.class);

    public MavenDependencyEditAction(MavenDependencySettingsService mavenDependencySettingsService)
    {
        this.mavenDependencySettingsService = mavenDependencySettingsService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        String key = request.param(MavenDependencyConfiguration.PARAM_KEY);
        String oldKey = request.param(MavenDependencyConfiguration.PARAM_OLD_KEY);
        String license = request.param(MavenDependencyConfiguration.PARAM_LICENSE);

        MavenDependency newMavenDependency = new MavenDependency(key, license);

        if (!mavenDependencySettingsService.hasDependency(newMavenDependency))
        {
            mavenDependencySettingsService.deleteMavenDependency(oldKey);
            mavenDependencySettingsService.addMavenDependency(newMavenDependency);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_OK);
            LOGGER.info(MavenDependencyConfiguration.INFO_EDIT_SUCCESS + newMavenDependency);
        }
        else
        {
            LOGGER.error(MavenDependencyConfiguration.ERROR_EDIT_ALREADY_EXISTS + newMavenDependency);
            response.stream().setStatus(HTTPConfiguration.HTTP_STATUS_NOT_MODIFIED);
        }
    }
}
