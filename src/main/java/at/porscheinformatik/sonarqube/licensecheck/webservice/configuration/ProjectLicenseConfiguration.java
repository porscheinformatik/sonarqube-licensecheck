package at.porscheinformatik.sonarqube.licensecheck.webservice.configuration;

public final class ProjectLicenseConfiguration
{
    private ProjectLicenseConfiguration()
    {

    }

    public static final String PARAM_PROJECT_KEY = "projectKey";
    public static final String PARAM_LICENSE = "license";
    public static final String PARAM_STATUS = "status";

    public static final String CONTROLLER = "api/licensecheck/project-licenses";

    public static final String SHOW_ACTION = "show";
    public static final String DELETE_ACTION = "delete";
    public static final String ADD_ACTION = "add";
    public static final String EDIT_ACTION = "edit";

    public static final String SHOW_ACTION_DESCRIPTION = "Show Project Licenses";
    public static final String DELETE_ACTION_DESCRIPTION = "Delete Project Licenses";
    public static final String ADD_ACTION_DESCRIPTION = "Add Project Licenses";
    public static final String EDIT_ACTION_DESCRIPTION = "Edit Project Licenses";

    public static final String ERROR_EDIT_ALREADY_EXISTS =
        "Edit Project License aborted. Project license already exists!";
    public static final String ERROR_EDIT_INVALID_INPUT = "Failed to edit project license , due to invalid input: ";
    public static final String INFO_EDIT_SUCCESS = "Project license edited";

    public static final String ERROR_ADD_ALREADY_EXISTS =
        "Add Project license aborted. Project license already exists!";
    public static final String INFO_ADD_SUCCESS = "Project license added: ";

    public static final String INFO_DELETE_SUCCESS = "Project license deleted: ";
}
