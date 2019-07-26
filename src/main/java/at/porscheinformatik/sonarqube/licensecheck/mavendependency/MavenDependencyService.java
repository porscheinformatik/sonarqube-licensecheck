package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.scanner.ScannerSide;

@ServerSide
@ScannerSide
public class MavenDependencyService
{
    private final Configuration configuration;

    public MavenDependencyService(Configuration configuration)
    {
        super();
        this.configuration = configuration;
    }

    public List<MavenDependency> getMavenDependencies()
    {
        final List<MavenDependency> mavenDependencies = new ArrayList<>();
        String dependencyString = configuration.get(ALLOWED_DEPENDENCIES_KEY).orElse("[]");

        JsonReader jsonReader = Json.createReader(new StringReader(dependencyString));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            mavenDependencies
                .add(new MavenDependency(jsonObject.getString("nameMatches"), jsonObject.getString("license")));
        }
        return mavenDependencies;
    }
}
