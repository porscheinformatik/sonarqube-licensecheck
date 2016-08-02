package at.porscheinformatik.sonarqube.licensecheck.webservice.mavendependency;

import java.util.List;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.text.JsonWriter;

import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.webservice.configuration.MavenDependencyConfiguration;

class MavenDependencyShowAction implements RequestHandler
{
    private final MavenDependencyService mavenDependencyService;

    public MavenDependencyShowAction(MavenDependencyService mavenDependencyService)
    {
        this.mavenDependencyService = mavenDependencyService;
    }

    @Override
    public void handle(Request request, Response response) throws Exception
    {
        final List<MavenDependency> mavenDependencies = mavenDependencyService.getMavenDependencies();

        JsonWriter json = response.newJsonWriter().beginObject();
        writeMavenDependencies(json, mavenDependencies);
        json.endObject().close();

    }

    private static void writeMavenDependencies(JsonWriter json, List<MavenDependency> mavenDependencies)
    {
        json.name(MavenDependencyConfiguration.JSON_ARRAY_NAME).beginArray();
        for (MavenDependency mavenDependency : mavenDependencies)
        {
            writeMavenDependency(json, mavenDependency);
        }
        json.endArray();
    }

    private static void writeMavenDependency(JsonWriter json, MavenDependency mavenDependency)
    {
        json
            .beginObject()
                .prop(MavenDependencyConfiguration.PROPERTY_KEY,
                    mavenDependency.getKey().isEmpty() ? null : mavenDependency.getKey())
                .prop(MavenDependencyConfiguration.PROPERTY_LICENSE,
                    mavenDependency.getLicense().isEmpty() ? null : mavenDependency.getLicense())
            .endObject();
    }

}
