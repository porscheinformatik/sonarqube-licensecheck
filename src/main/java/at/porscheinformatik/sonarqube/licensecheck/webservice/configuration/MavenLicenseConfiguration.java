package at.porscheinformatik.sonarqube.licensecheck.webservice.configuration;

public final class MavenLicenseConfiguration extends LicenseCheckConfiguration
{
    private MavenLicenseConfiguration()
    {

    }

    public static final String PARAM_REGEX = "regex";
    public static final String PARAM_OLD_REGEX = "old_regex";
    public static final String JSON_ARRAY_NAME = "mavenLicenses";
    public static final String CONTROLLER = "api/licensecheck/maven-licenses";

    public static final String SHOW_ACTION_DESCRIPTION = "Show Maven Licenses";
    public static final String DELETE_ACTION_DESCRIPTION = "Delete Maven License";
    public static final String ADD_ACTION_DESCRIPTION = "Add Maven License";
    public static final String EDIT_ACTION_DESCRIPTION = "Edit Maven License";

    public static final String PROPERTY_REGEX = "regex";

    public static final String ERROR_EDIT_ALREADY_EXISTS = "Edit Maven License aborted. Maven License already exists: ";
    public static final String INFO_EDIT_SUCCESS = "Maven License edited: ";

    public static final String ERROR_ADD_ALREADY_EXISTS = "Add Maven License aborted. License REGEX already exists: ";
    public static final String INFO_ADD_SUCCESS = "Maven License added: ";

    public static final String INFO_DELETE_SUCCESS = "Maven License deleted: ";
}
