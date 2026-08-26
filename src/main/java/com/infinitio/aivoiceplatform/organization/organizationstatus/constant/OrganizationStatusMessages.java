package com.infinitio.aivoiceplatform.organization.organizationstatus.constant;

/**
 * Organization Status Messages.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class OrganizationStatusMessages {

    private OrganizationStatusMessages() {
    }

    public static final String CREATED =
            "Organization status created successfully.";

    public static final String UPDATED =
            "Organization status updated successfully.";

    public static final String DELETED =
            "Organization status deleted successfully.";

    public static final String ACTIVATED =
            "Organization status activated successfully.";

    public static final String DEACTIVATED =
            "Organization status deactivated successfully.";

    public static final String NOT_FOUND =
            "Organization status not found.";

    public static final String CODE_ALREADY_EXISTS =
            "Organization status code already exists.";

    public static final String NAME_ALREADY_EXISTS =
            "Organization status name already exists.";

    public static final String INVALID_PUBLIC_ID =
            "Organization status public ID is required.";

    public static final String INVALID_DISPLAY_ORDER =
            "Display order must be greater than zero.";
}