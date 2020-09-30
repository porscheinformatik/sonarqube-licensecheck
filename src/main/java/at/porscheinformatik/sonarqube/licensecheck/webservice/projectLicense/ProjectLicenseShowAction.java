package at.porscheinformatik.sonarqube.licensecheck.webservice.projectLicense;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicense;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseService;

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

        OutputStream os = response.stream()
            .setStatus(200)
            .setMediaType("application/json")
            .output();

        try (Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8))
        {
            out.write(ProjectLicense.createString(projectLicenses));
        }
    }
}
