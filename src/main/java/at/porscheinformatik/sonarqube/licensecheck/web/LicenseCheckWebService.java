package at.porscheinformatik.sonarqube.licensecheck.web;

import org.sonar.api.server.ws.WebService;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencySettingsService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseSettingsService;
import at.porscheinformatik.sonarqube.licensecheck.projectlicense.ProjectLicenseSettingsService;

/**
 * Web service for license-check rest API.
 */
public class LicenseCheckWebService implements WebService
{
    private final LicenseSettingsService licenseSettingsService;
    private final ProjectLicenseSettingsService projectLicenseSettingsService;
    private final MavenLicenseSettingsService mavenLicenseSettingsService;
    private final MavenDependencySettingsService mavenDependencySettingsService;

    public LicenseCheckWebService(LicenseSettingsService licenseSettingsService,
        ProjectLicenseSettingsService projectLicenseSettingsService,
        MavenLicenseSettingsService mavenLicenseSettingsService,
        MavenDependencySettingsService mavenDependencySettingsService)
    {
        super();
        this.licenseSettingsService = licenseSettingsService;
        this.projectLicenseSettingsService = projectLicenseSettingsService;
        this.mavenLicenseSettingsService = mavenLicenseSettingsService;
        this.mavenDependencySettingsService = mavenDependencySettingsService;
    }

    @Override
    public void define(Context context)
    {
        // Init all services
        licenseSettingsService.init();
        projectLicenseSettingsService.init();
        mavenLicenseSettingsService.init();
        mavenDependencySettingsService.init();
    }
}
