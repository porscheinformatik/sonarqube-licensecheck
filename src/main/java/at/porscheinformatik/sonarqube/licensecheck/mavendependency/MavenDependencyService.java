package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;

@ServerSide
@ScannerSide
public class MavenDependencyService
{
    private final Settings settings;

    public MavenDependencyService(Settings settings)
    {
        super();
        this.settings = settings;
    }

    public List<MavenDependency> getMavenDependencies()
    {
        final List<MavenDependency> mavenDependencies = new ArrayList<>();
        String dependencyString = settings.getString(ALLOWED_DEPENDENCIES_KEY);
        initMavenDependencyData(dependencyString);

        JsonReader jsonReader = Json.createReader(new StringReader(settings.getString(ALLOWED_DEPENDENCIES_KEY)));
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

    private void initMavenDependencyData(String mavenDependencyString)
    {
        if ((mavenDependencyString == null) || mavenDependencyString.isEmpty())
        {
            JsonArray jsonArray = Json
                .createArrayBuilder()
                .add(Json.createObjectBuilder().add("nameMatches", "org.apache..*").add("license", "Apache-2.0"))
                .build();
            String initValueMavenDepependencies = jsonArray.toString();

            settings.setProperty(ALLOWED_DEPENDENCIES_KEY, initValueMavenDepependencies);
        }
    }
}
