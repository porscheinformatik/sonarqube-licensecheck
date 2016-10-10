package at.porscheinformatik.sonarqube.licensecheck.widget;

import java.util.List;

import org.sonar.api.server.ServerSide;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

@ServerSide
public class WidgetHelper
{
    private final LicenseService licenseService;

    public WidgetHelper(LicenseService licenseService)
    {
        super();
        this.licenseService = licenseService;
    }

    public List<License> getLicenses(String licensesMeasure)
    {
        return License.fromString(licensesMeasure);
    }

    public List<Dependency> getDependencies(String dependencyMeasure)
    {
        List<License> licenses = licenseService.getLicenses();
        List<Dependency> dependencies = Dependency.fromString(dependencyMeasure);
        for (Dependency dependency : dependencies)
        {
            for (License license : licenses)
            {
                if (license.getIdentifier().equals(dependency.getLicense()))
                {
                    dependency.setStatus(license.getStatus());
                    break;
                }
            }
        }
        return dependencies;
    }
}
