package at.porscheinformatik.sonarqube.licensecheck.webservice.license;

import java.util.List;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.text.JsonWriter;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.LicenseConfiguration;

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

        JsonWriter json = response.newJsonWriter().beginObject();
        writeLicenses(json, licenses);
        json.endObject().close();
    }

    private static void writeLicenses(JsonWriter json, List<License> licenses)
    {
        json.name(LicenseConfiguration.JSON_ARRAY_NAME).beginArray();
        for (License license : licenses)
        {
            writeLicense(json, license);
        }
        json.endArray();
    }

    private static void writeLicense(JsonWriter json, License license)
    {
        json
            .beginObject()
            .prop(LicenseConfiguration.PROPERTY_NAME, license.getName().isEmpty() ? null : license.getName())
            .prop(LicenseConfiguration.PROPERTY_IDENTIFIER,
                license.getIdentifier().isEmpty() ? null : license.getIdentifier())
            .prop(LicenseConfiguration.PROPERTY_STATUS, license.getStatus().isEmpty() ? null : license.getStatus())
            .endObject();
    }
}
