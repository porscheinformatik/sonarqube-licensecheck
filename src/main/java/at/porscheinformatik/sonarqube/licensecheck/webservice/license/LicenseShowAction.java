package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

class LicenseShowAction implements RequestHandler
{
    private final LicenseService licenseService;

    public LicenseShowAction(LicenseService licenseService)
    {
        this.licenseService = licenseService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        final List<License> licenses = licenseService.getLicenses();

        OutputStream os = response.stream()
            .setStatus(200)
            .setMediaType("application/json")
            .output();

        try (Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8))
        {
            out.write(License.createString(licenses));
        }
    }
}
