package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.MAVEN_DEPENDENCY_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency.FIELD_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency.FIELD_LICENSE;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;

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
        return Arrays.stream(configuration.getStringArray(MAVEN_DEPENDENCY_MAPPING))
            .map(idx -> {
                String idxProp = "." + idx + ".";
                String key = configuration.get(MAVEN_DEPENDENCY_MAPPING + idxProp + FIELD_KEY).orElse(null);
                String license = configuration.get(MAVEN_DEPENDENCY_MAPPING + idxProp + FIELD_LICENSE).orElse(null);
                return new MavenDependency(key, license);
            })
            .collect(Collectors.toList());
    }

    /**
     * @return maven dependencies via deprecated setting
     * @deprecated use {@link #getMavenDependencies()} instead
     */
    @Deprecated
    public List<MavenDependency> getMavenDependenciesOld()
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
