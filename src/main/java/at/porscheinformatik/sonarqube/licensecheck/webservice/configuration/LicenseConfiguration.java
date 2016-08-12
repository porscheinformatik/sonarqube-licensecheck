package at.porscheinformatik.sonarqube.licensecheck.webservice.configuration;

public final class LicenseConfiguration
{
    public static final String PARAM = "license";
    public static final String JSON_ARRAY_NAME = "licenses";
    public static final String CONTROLLER = "api/licenses";

    public static final String SHOW_ACTION = "show";
    public static final String DELETE_ACTION = "delete";
    public static final String ADD_ACTION = "add";
    public static final String EDIT_ACTION = "edit";

    public static final String SHOW_ACTION_DESCRIPTION = "Show Licenses";
    public static final String DELETE_ACTION_DESCRIPTION = "Delete Licenses";
    public static final String ADD_ACTION_DESCRIPTION = "Add Licenses";
    public static final String EDIT_ACTION_DESCRIPTION = "Edit Licenses";

    public static final String PROPERTY_NEW_NAME = "newName";
    public static final String PROPERTY_NEW_IDENTIFIER = "newIdentifier";
    public static final String PROPERTY_NEW_STATUS = "newStatus";
    public static final String PROPERTY_OLD_NAME = "oldName";
    public static final String PROPERTY_OLD_IDENTIFIER = "oldIdentifier";
    public static final String PROPERTY_OLD_STATUS = "oldStatus";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_IDENTIFIER = "identifier";
    public static final String PROPERTY_STATUS = "status";

    public static final String ERROR_EDIT_ALREADY_EXISTS = "Edit License aborted. License already exists!";
    public static final String ERROR_EDIT_INVALID_INPUT = "Failed to edit license, due to invalid input: ";
    public static final String INFO_EDIT_SUCCESS = "License edited";

    public static final String ERROR_ADD_ALREADY_EXISTS = "Add License aborted. License already exists!";
    public static final String ERROR_ADD_INVALID_INPUT = "Failed to add license, due to invalid input: ";
    public static final String INFO_ADD_SUCCESS = "License added: ";

    public static final String ERROR_DELETE_INVALID_INPUT = "Failed to delete license, due to invalid identifier: ";
    public static final String INFO_DELETE_SUCCESS = "License deleted: ";
}
