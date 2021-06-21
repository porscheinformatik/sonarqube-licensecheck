package at.porscheinformatik.sonarqube.licensecheck.web;

import org.sonar.api.server.ws.WebService;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMappingSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseSettingsService;

/**
 * Web service for license-check rest API.
 */
public class LicenseCheckWebService implements WebService
{
    private final LicenseSettingsService licenseSettingsService;
    private final ProjectLicenseSettingsService projectLicenseSettingsService;
    private final LicenseMappingSettingsService licenseMappingSettingsService;
    private final DependencyMappingSettingsService dependencyMappingSettingsService;

    public LicenseCheckWebService(LicenseSettingsService licenseSettingsService,
        ProjectLicenseSettingsService projectLicenseSettingsService,
        LicenseMappingSettingsService licenseMappingSettingsService,
        DependencyMappingSettingsService dependencyMappingSettingsService)
    {
        super();
        this.licenseSettingsService = licenseSettingsService;
        this.projectLicenseSettingsService = projectLicenseSettingsService;
        this.licenseMappingSettingsService = licenseMappingSettingsService;
        this.dependencyMappingSettingsService = dependencyMappingSettingsService;
    }

    @Override
    public void define(Context context)
    {
        // Init all services
        licenseSettingsService.init();
        projectLicenseSettingsService.init();
        licenseMappingSettingsService.init();
        dependencyMappingSettingsService.init();
    }
}
