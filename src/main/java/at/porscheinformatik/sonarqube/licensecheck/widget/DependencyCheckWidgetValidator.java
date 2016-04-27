package at.porscheinformatik.sonarqube.licensecheck.widget;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.server.ServerSide;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

@ServerSide
public class DependencyCheckWidgetValidator
{
    private final LicenseService licenseService;
    private String dependencyString;
    private String licenseString;

    public DependencyCheckWidgetValidator(LicenseService licenseService)
    {
        super();
        this.licenseService = licenseService;
    }

    private List<License> getAllowedLicensesList()
    {
        List<License> allowedLicensesList = new ArrayList<>();
        for (License license : licenseService.getLicenses())
        {
            if ("true".equals(license.getStatus()))
            {
                allowedLicensesList.add(new License(license.getName(), license.getIdentifier(), license.getStatus()));
            }
        }
        return allowedLicensesList;
    }

    public String validateLicense(String dependency)
    {
        String caseString = "";

        List<License> allowedLicenses = getAllowedLicensesList();
        String[] splittedDependency = dependency.split("~");

        String licenseFromDependency = splittedDependency[2];
        for (License license : allowedLicenses)
        {
            if (license.getIdentifier().equals(licenseFromDependency))
            {
                caseString = "ok";
                break;
            }
            else
            {
                caseString = "warn";
            }
        }
        return caseString;
    }

    public void setDependencyString(String dependency)
    {
        dependencyString = dependency;
    }

    public String getDependencyString()
    {
        return dependencyString;
    }

    public void setLicenseString(String license)
    {
        licenseString = license;
    }

    public String getLicenseString()
    {
        return licenseString;
    }
}
