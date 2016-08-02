package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

@ServerSide
public class MavenDependencySettingsService
{
    /** This is not official API */
    private final PersistentSettings persistentSettings;

    private final Settings settings;
    private final MavenDependencyService mavenDependencyService;

    public MavenDependencySettingsService(PersistentSettings persistentSettings,
        MavenDependencyService mavenDependencyService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.settings = persistentSettings.getSettings();
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

        if (!checkIfListContains(newMavenDependency))
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

    public boolean checkIfListContains(MavenDependency mavenDependency)
    {
        List<MavenDependency> mavenDependencies = mavenDependencyService.getMavenDependencies();
        return mavenDependencies.contains(mavenDependency);
    }

    public void deleteMavenDependency(String key)
    {
        String[] keyId = key.split("~");

        List<MavenDependency> newDependencyList = new ArrayList<>();
        JsonReader jsonReader = Json.createReader(new StringReader(settings.getString(ALLOWED_DEPENDENCIES_KEY)));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            if (!jsonObject.getString("nameMatches").equals(keyId[0]))
            {
                newDependencyList
                    .add(new MavenDependency(jsonObject.getString("nameMatches"), jsonObject.getString("license")));
            }
        }
        saveSettings(newDependencyList);
    }

    private void saveSettings(List<MavenDependency> mavenDependencies)
    {
        String newJsonDependency = "";

        for (int i = 0; i < mavenDependencies.size(); i++)
        {
            JsonArray jsonArray = Json
                .createArrayBuilder()
                .add(Json.createObjectBuilder().add("nameMatches", mavenDependencies.get(i).getKey()).add("license",
                    mavenDependencies.get(i).getLicense()))
                .build();
            newJsonDependency = newJsonDependency + jsonArray.toString();

            if (newJsonDependency.contains("]["))
            {
                newJsonDependency = newJsonDependency.replace("][", ", ");
            }
        }
        settings.setProperty(ALLOWED_DEPENDENCIES_KEY, newJsonDependency);
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

        String mavenDependencies = settings.getString(ALLOWED_DEPENDENCIES_KEY);

        if ((mavenDependencies != null) && !mavenDependencies.isEmpty())
        {
            return;
        }

        settings.setProperty(ALLOWED_DEPENDENCIES_KEY, initValueMavenDepependencies);
        persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, initValueMavenDepependencies);
    }

    public void sortDependencies()
    {
        List<MavenDependency> mavenDependencies = mavenDependencyService.getMavenDependencies();
        Collections.sort(mavenDependencies);
        saveSettings(mavenDependencies);
    }
}
