package at.porscheinformatik.sonarqube.licensecheck.webservice.configuration;

public final class MavenDependencyConfiguration
{
    public static final String PARAM_KEY = "key";
    public static final String PARAM_LICENSE = "license";
    public static final String PARAM_OLD_KEY = "old_key";
    public static final String JSON_ARRAY_NAME = "mavenDependencies";
    public static final String CONTROLLER = "api/licensecheck/maven-dependencies";

    public static final String SHOW_ACTION = "show";
    public static final String DELETE_ACTION = "delete";
    public static final String ADD_ACTION = "add";
    public static final String EDIT_ACTION = "edit";

    public static final String SHOW_ACTION_DESCRIPTION = "Show Maven Dependencies";
    public static final String DELETE_ACTION_DESCRIPTION = "Delete Maven Dependency";
    public static final String ADD_ACTION_DESCRIPTION = "Add Maven Dependency";
    public static final String EDIT_ACTION_DESCRIPTION = "Edit Maven Dependency";

    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_LICENSE = "license";

    public static final String ERROR_EDIT_ALREADY_EXISTS = "Edit Maven Dependency aborted. Maven Dependency already exists: ";
    public static final String ERROR_EDIT_INVALID_INPUT = "Failed to edit maven dependency, due to invalid input: ";
    public static final String INFO_EDIT_SUCCESS = "Maven Dependency edited: ";

    public static final String ERROR_ADD_ALREADY_EXISTS =
        "Add Maven Dependency aborted. Maven Dependency already exists: ";
    public static final String ERROR_ADD_INVALID_INPUT = "Failed to add maven dependency, due to invalid input: ";
    public static final String INFO_ADD_SUCCESS = "Maven Dependency added: ";

    public static final String ERROR_DELETE_INVALID_INPUT = "Failed to delete maven dependency, due to invalid key: ";
    public static final String INFO_DELETE_SUCCESS = "Maven Dependency deleted: ";
}
