package at.porscheinformatik.sonarqube.licensecheck.dependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

@ServerSide
public class DependencySettingsService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencySettingsService.class);

    /** This is not official API */
    private final PersistentSettings persistentSettings;

    private final Settings settings;
    private final DependencyService dependencyService;

    public DependencySettingsService(PersistentSettings persistentSettings, DependencyService dependencyService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.settings = persistentSettings.getSettings();
        this.dependencyService = dependencyService;
        initAllowedDependencies();
    }

    public String getAllowedDependenciesForTable()
    {
        List<AllowedDependency> allowedDependencyList = dependencyService.getAllowedDependencies();
        StringBuilder allowedDependencyString = new StringBuilder();
        for (AllowedDependency allowedDependency : allowedDependencyList)
        {
            allowedDependencyString.append(allowedDependency.getKey()).append("~");
            allowedDependencyString.append(allowedDependency.getLicense()).append(";");
        }
        return allowedDependencyString.toString();
    }

    public void addAllowedDependency(String key, String license)
    {
        String allowedDependencyString = settings.getString(ALLOWED_DEPENDENCIES_KEY);

        if (!allowedDependencyString.contains(key))
        {
            String newJsonDependency = "";
            JsonArray jsonArray = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("nameMatches", key)
                    .add("license", license))
                .build();
            newJsonDependency = newJsonDependency + jsonArray.toString();

            allowedDependencyString = allowedDependencyString + newJsonDependency;
            if (allowedDependencyString.contains("]["))
            {
                String addedAllowedDependencies = allowedDependencyString.replace("][", ", ");
                settings.setProperty(ALLOWED_DEPENDENCIES_KEY, addedAllowedDependencies);
                persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, addedAllowedDependencies);
            }
            else
            {
                settings.setProperty(ALLOWED_DEPENDENCIES_KEY, allowedDependencyString);
                persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, allowedDependencyString);
            }
        }
        else
        {
            LOGGER.info("Dependency already exists!");
        }
    }

    public void deleteAllowedDependency(String key)
    {
        String[] keyId = key.split("~");

        List<AllowedDependency> newDependencyList = new ArrayList<>();
        JsonReader jsonReader = Json.createReader(new StringReader(settings.getString(ALLOWED_DEPENDENCIES_KEY)));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            if (!jsonObject.getString("nameMatches").equals(keyId[0]))
            {
                newDependencyList.add(
                    new AllowedDependency(jsonObject.getString("nameMatches"), jsonObject.getString("license")));
            }
        }
        saveSettings(newDependencyList);
    }

    private void saveSettings(List<AllowedDependency> allowedDependencyList)
    {
        String newJsonDependency = "";

        for (int i = 0; i < allowedDependencyList.size(); i++)
        {
            JsonArray jsonArray = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("nameMatches", allowedDependencyList.get(i).getKey())
                    .add("license", allowedDependencyList.get(i).getLicense()))
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

    private void initAllowedDependencies()
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

        String allowedDependencies = settings.getString(ALLOWED_DEPENDENCIES_KEY);

        if ((allowedDependencies != null) && !allowedDependencies.isEmpty())
        {
            return;
        }

        settings.setProperty(ALLOWED_DEPENDENCIES_KEY, initValueAllowedDepependencies);
        persistentSettings.saveProperty(ALLOWED_DEPENDENCIES_KEY, initValueAllowedDepependencies);
    }
}
