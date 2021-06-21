package at.porscheinformatik.sonarqube.licensecheck.dependencymapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.DEPENDENCY_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_OVERWRITE;

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
public class DependencyMappingService
{
    private final Configuration configuration;

    public DependencyMappingService(Configuration configuration)
    {
        super();
        this.configuration = configuration;
    }

    public List<DependencyMapping> getDependencyMappings()
    {
        return Arrays.stream(configuration.getStringArray(DEPENDENCY_MAPPING))
            .map(idx -> {
                String idxProp = "." + idx + ".";
                String key = configuration.get(DEPENDENCY_MAPPING + idxProp + FIELD_KEY).orElse(null);
                String license = configuration.get(DEPENDENCY_MAPPING + idxProp + FIELD_LICENSE).orElse(null);
                Boolean overwrite =
                    configuration.getBoolean(DEPENDENCY_MAPPING + idxProp + FIELD_OVERWRITE).orElse(Boolean.FALSE);
                return new DependencyMapping(key, license, overwrite);
            })
            .collect(Collectors.toList());
    }

    /**
     * @return maven dependencies via deprecated setting
     * @deprecated use {@link #getDependencyMappings()} instead
     */
    @Deprecated
    public List<DependencyMapping> getDependencyMappingsOld()
    {
        final List<DependencyMapping> dependencyMappings = new ArrayList<>();
        String dependencyString = configuration.get(ALLOWED_DEPENDENCIES_KEY).orElse("[]");

        JsonReader jsonReader = Json.createReader(new StringReader(dependencyString));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        for (int i = 0; i < jsonArray.size(); i++)
        {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            dependencyMappings.add(
                new DependencyMapping(jsonObject.getString("nameMatches"), jsonObject.getString("license"), true));
        }
        return dependencyMappings;
    }
}
