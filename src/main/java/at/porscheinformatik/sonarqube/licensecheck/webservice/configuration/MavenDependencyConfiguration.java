package at.porscheinformatik.sonarqube.licensecheck.webservice.configuration;

public final class MavenDependencyConfiguration extends LicenseCheckConfiguration
{
    private MavenDependencyConfiguration()
    {

    }

    public static final String PARAM_KEY = "key";
    public static final String PARAM_OLD_KEY = "old_key";
    public static final String JSON_ARRAY_NAME = "mavenDependencies";
    public static final String CONTROLLER = "api/licensecheck/maven-dependencies";

    public static final String SHOW_ACTION_DESCRIPTION = "Show Maven Dependencies";
    public static final String ADD_ACTION_DESCRIPTION = "Add Maven Dependency";
    public static final String EDIT_ACTION_DESCRIPTION = "Edit Maven Dependency";

    public static final String PROPERTY_KEY = "key";

    public static final String ERROR_EDIT_ALREADY_EXISTS = "Edit Maven Dependency aborted. Maven Dependency already exists: ";
    public static final String INFO_EDIT_SUCCESS = "Maven Dependency edited: ";

    public static final String ERROR_ADD_ALREADY_EXISTS =
        "Add Maven Dependency aborted. Maven Dependency already exists: ";

    public static final String INFO_DELETE_SUCCESS = "Maven Dependency deleted: ";
}
