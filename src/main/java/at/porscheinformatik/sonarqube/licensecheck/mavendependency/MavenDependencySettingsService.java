package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

@ServerSide
public class MavenDependencySettingsService
{
    private final Configuration configuration;

    /**
     * This is not official API
     */
    private final PersistentSettings persistentSettings;

    private final MavenDependencyService mavenDependencyService;

    public MavenDependencySettingsService(Configuration configuration, PersistentSettings persistentSettings,
        MavenDependencyService mavenDependencyService)
    {
        super();
        this.configuration = configuration;
        this.persistentSettings = persistentSettings;
        this.mavenDependencyService = mavenDependencyService;
        initMavenDependencies();
    }

    public boolean addMavenDependency(String key, String license)
    {
        MavenDependency newMavenDependency = new MavenDependency(key, license);
        return addMavenDependency(newMavenDependency);
    }

    public boolean addMavenDependency(MavenDependency newMavenDependency)
    {
        List<MavenDependency> mavenDependencies = mavenDependencyService.getMavenDependencies();

        if (!mavenDependencies.contains(newMavenDependency))
        {
            mavenDependencies.add(newMavenDependency);
            saveSettings(mavenDependencies);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void deleteMavenDependency(final String key)
    {
        List<MavenDependency> newDependencyList = new ArrayList<>();
        JsonReader jsonReader =
            Json.createReader(new StringReader(configuration.get(ALLOWED_DEPENDENCIES_KEY).orElse("")));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            if (!jsonObject.getString("nameMatches").equals(key))
            {
                newDependencyList
                    .add(new MavenDependency(jsonObject.getString("nameMatches"), jsonObject.getString("license")));
            }
        }
        saveSettings(newDependencyList);
    }

    private void saveSettings(List<MavenDependency> mavenDependencies)
    {
        Collections.sort(mavenDependencies);

        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        mavenDependencies.stream()
            .map(mavenDependency ->
                Json.createObjectBuilder()
                    .add("nameMatches", mavenDependency.getKey())
                    .add("license", mavenDependency.getLicense()))
            .forEach(jsonArray::add);

        String newJsonDependency = jsonArray.build().toString();
        persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, newJsonDependency);
    }

    private void initMavenDependencies()
    {
        JsonArray jsonArray = Json
            .createArrayBuilder()
            .add(Json.createObjectBuilder().add("nameMatches", "org.apache..*").add("license", "Apache-2.0"))
            .add(Json.createObjectBuilder().add("nameMatches", "org.glassfish..*").add("license", "CDDL-1.0"))
            .build();
        String initValueMavenDepependencies = jsonArray.toString();

        String mavenDependencies = configuration.get(ALLOWED_DEPENDENCIES_KEY).orElse(null);

        if ((mavenDependencies != null) && !mavenDependencies.isEmpty())
        {
            return;
        }

        persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, initValueMavenDepependencies);
    }

    public boolean hasDependency(MavenDependency mavenDependency)
    {
        return mavenDependencyService.getMavenDependencies().contains(mavenDependency);
    }
}
