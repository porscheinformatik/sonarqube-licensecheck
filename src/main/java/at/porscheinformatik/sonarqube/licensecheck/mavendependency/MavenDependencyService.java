package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

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
        String dependencyString = configuration.get(ALLOWED_DEPENDENCIES_KEY).orElse("[]").replaceAll(
            LicenseService.COMMA_PLACEHOLDER, ",");

        if (dependencyString.isEmpty())
        {
            dependencyString = "[]";
        }

        JsonReader jsonReader = Json.createReader(new StringReader(dependencyString));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            //TODO: May rename nameMatches to "key" or "key" to nameMatches but this renaming in code (especially in JS) seems useless.
            mavenDependencies
                .add(new MavenDependency(jsonObject.getString("nameMatches"), jsonObject.getString("license")));
        }

        if(mavenDependencies.isEmpty())
        {
            //TODO: Create a file for those defaults also and use defaultValue in property definition than.
            return new ArrayList<>() {
                {
                    add(new MavenDependency("org.apache..*", "Apache-2.0"));
                    add(new MavenDependency("org.glassfish..*", "CDDL-1.0"));
                }
            };
        }

        return mavenDependencies;
    }
}
