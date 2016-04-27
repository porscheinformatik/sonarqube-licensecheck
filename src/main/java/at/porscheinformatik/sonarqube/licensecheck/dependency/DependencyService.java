package at.porscheinformatik.sonarqube.licensecheck.dependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.batch.BatchSide;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;

@ServerSide
@BatchSide
public class DependencyService
{
    private final Settings settings;

    public DependencyService(Settings settings)
    {
        super();
        this.settings = settings;
    }

    public List<AllowedDependency> getAllowedDependencies()
    {
        final List<AllowedDependency> allowedDependenciesList = new ArrayList<>();
        String dependencyString = settings.getString(ALLOWED_DEPENDENCIES_KEY);
        initDependencyData(dependencyString);

        JsonReader jsonReader = Json.createReader(new StringReader(settings.getString(ALLOWED_DEPENDENCIES_KEY)));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            allowedDependenciesList
                .add(new AllowedDependency(jsonObject.getString("nameMatches"), jsonObject.getString("license")));
        }
        return allowedDependenciesList;
    }

    private void initDependencyData(String dependencyString)
    {
        if ((dependencyString == null) || dependencyString.isEmpty())
        {
            JsonArray jsonArray = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("nameMatches", "org.apache..*")
                    .add("license", "Apache-2.0"))
                .add(Json.createObjectBuilder()
                    .add("nameMatches", "org.glassfish..*")
                    .add("license", "CDDL-1.0"))
                .build();
            String initValueAllowedDepependencies = jsonArray.toString();

            settings.setProperty(ALLOWED_DEPENDENCIES_KEY, initValueAllowedDepependencies);
        }
    }
}
