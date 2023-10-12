package at.porscheinformatik.sonarqube.licensecheck.dependencymapping;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.DEPENDENCY_MAPPING;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping.FIELD_OVERWRITE;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

}
