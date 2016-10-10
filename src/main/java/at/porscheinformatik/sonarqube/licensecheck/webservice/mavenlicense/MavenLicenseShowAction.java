package at.porscheinformatik.sonarqube.licensecheck.webservice.mavenlicense;

import java.util.List;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.text.JsonWriter;

import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicense;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenLicenseConfiguration;

class MavenLicenseShowAction implements RequestHandler
{
    private final MavenLicenseService mavenLicenseService;

    public MavenLicenseShowAction(MavenLicenseService mavenLicenseService)
    {
        this.mavenLicenseService = mavenLicenseService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        final List<MavenLicense> mavenLicenses = mavenLicenseService.getMavenLicenseList();

        JsonWriter json = response.newJsonWriter().beginObject();
        writeMavenLicenses(json, mavenLicenses);
        json.endObject().close();

    }

    private static void writeMavenLicenses(JsonWriter json, List<MavenLicense> mavenLicenses)
    {
        json.name(MavenLicenseConfiguration.JSON_ARRAY_NAME).beginArray();
        for (MavenLicense mavenLicense : mavenLicenses)
        {
            writeMavenLicense(json, mavenLicense);
        }
        json.endArray();
    }

    private static void writeMavenLicense(JsonWriter json, MavenLicense mavenLicense)
    {
        json
            .beginObject()
                .prop(MavenLicenseConfiguration.PROPERTY_REGEX, mavenLicense.getLicenseNameRegEx().toString().isEmpty()
                    ? null : mavenLicense.getLicenseNameRegEx().toString())
                .prop(MavenLicenseConfiguration.PROPERTY_KEY,
                    mavenLicense.getLicense().isEmpty() ? null : mavenLicense.getLicense())
            .endObject();
    }

}
