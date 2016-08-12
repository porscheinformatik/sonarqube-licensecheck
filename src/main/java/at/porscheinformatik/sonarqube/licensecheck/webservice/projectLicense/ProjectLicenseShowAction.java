package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

import java.util.List;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.text.JsonWriter;

import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectLicense.ProjectLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.ProjectLicenseConfiguration;

class ProjectLicenseShowAction implements RequestHandler
{

    private final ProjectLicenseService projectLicenseService;

    public ProjectLicenseShowAction(ProjectLicenseService projectLicenseService)
    {
        this.projectLicenseService = projectLicenseService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        final List<ProjectLicense> projectLicenses = projectLicenseService.getProjectLicenseList();

        JsonWriter json = response.newJsonWriter().beginObject();
        writeProjects(json, projectLicenses);
        json.endObject().close();
    }

    private static void writeProjects(JsonWriter json, List<ProjectLicense> projectLicenses)
    {
        json.name(ProjectLicenseConfiguration.JSON_ARRAY_NAME).beginArray();
        for (ProjectLicense projectLicense : projectLicenses)
        {
            writeProject(json, projectLicense);
        }
        json.endArray();
    }

    private static void writeProject(JsonWriter json, ProjectLicense projectLicense)
    {
        json
            .beginObject()
            .prop(ProjectLicenseConfiguration.PROPERTY_LICENSE,
                projectLicense.getLicense().isEmpty() ? null : projectLicense.getLicense())
            .prop(ProjectLicenseConfiguration.PROPERTY_PROJECT_NAME,
                projectLicense.getProjectName().isEmpty() ? null : projectLicense.getProjectName())
            .prop(ProjectLicenseConfiguration.PROPERTY_STATUS,
                projectLicense.getStatus().isEmpty() ? null : projectLicense.getStatus())
            .endObject();
    }
}
