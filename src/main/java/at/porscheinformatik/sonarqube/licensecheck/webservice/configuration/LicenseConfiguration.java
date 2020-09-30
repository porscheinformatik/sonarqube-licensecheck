package at.porscheinformatik.sonarqube.licensecheck.webservice.configuration;

public final class LicenseConfiguration
{
    private LicenseConfiguration()
    {

    }

    public static final String PARAM_NAME = "name";
    public static final String PARAM_IDENTIFIER = "identifier";
    public static final String PARAM_STATUS = "status";

    public static final String CONTROLLER = "api/licensecheck/licenses";

    public static final String SHOW_ACTION = "show";
    public static final String DELETE_ACTION = "delete";
    public static final String ADD_ACTION = "add";
    public static final String EDIT_ACTION = "edit";

    public static final String SHOW_ACTION_DESCRIPTION = "Show Licenses";
    public static final String DELETE_ACTION_DESCRIPTION = "Delete Licenses";
    public static final String ADD_ACTION_DESCRIPTION = "Add Licenses";
    public static final String EDIT_ACTION_DESCRIPTION = "Edit Licenses";

    public static final String INFO_EDIT_SUCCESS = "License edited id:{}, new name {}, new status {}";

    public static final String ERROR_ADD_ALREADY_EXISTS = "Add License aborted. License already exists!";
    public static final String INFO_ADD_SUCCESS = "License added: ";

    public static final String ERROR_DELETE_LICENSE = "Failed to delete license: {}";
    public static final String INFO_DELETE_SUCCESS = "License deleted: {}";
}
