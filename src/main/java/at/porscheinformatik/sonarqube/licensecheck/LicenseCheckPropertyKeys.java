package at.porscheinformatik.sonarqube.licensecheck;

public class LicenseCheckPropertyKeys
{
    /**
     * Category for all settings.
     */
    public static final String CATEGORY = "License Check";

    /**
     * Config key for activating/deactivating the plugin
     */
    public static final String ACTIVATION_KEY = "licensecheck.activation";

    /**
     * Config key for the list of licenses
     */
    public static final String LICENSE_SET = "licensecheck.license-set";

    /**
     * Config key for the list of project specific allowed/disallowed licenses
     */
    public static final String PROJECT_LICENSE_SET = "licensecheck.project-license-set";

    /**
     * Config key the Maven license name mapping
     */
    public static final String LICENSE_MAPPING = "licensecheck.license-mapping";

    /**
     * Config key the Maven dependency to license mapping
     */
    public static final String DEPENDENCY_MAPPING = "licensecheck.dep-mapping";

    /**
     * Config key to enable/disable transitive dependencies for NPM
     */
    public static final String NPM_RESOLVE_TRANSITIVE_DEPS = "licensecheck.npm.resolvetransitive";


    private LicenseCheckPropertyKeys()
    {
    }
}
