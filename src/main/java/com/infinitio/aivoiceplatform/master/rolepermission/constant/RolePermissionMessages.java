package com.infinitio.aivoiceplatform.master.rolepermission.constant;

/**
 * Role Permission Messages.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class RolePermissionMessages {

    private RolePermissionMessages() {
    }


    // =========================================================
    // SUCCESS
    // =========================================================

    public static final String CREATED =
            "Role permission mapping created successfully.";

    public static final String UPDATED =
            "Role permission mapping updated successfully.";

    public static final String DELETED =
            "Role permission mapping deleted successfully.";

    public static final String ACTIVATED =
            "Role permission mapping activated successfully.";

    public static final String DEACTIVATED =
            "Role permission mapping deactivated successfully.";

    public static final String FETCHED =
            "Role permission mapping fetched successfully.";

    public static final String FETCHED_ALL =
            "Role permission mappings fetched successfully.";


    // =========================================================
    // ERROR
    // =========================================================

    public static final String NOT_FOUND =
            "Role permission mapping not found.";

    public static final String ALREADY_EXISTS =
            "This permission is already assigned to the role.";

    public static final String INVALID_ROLE =
            "Invalid role.";

    public static final String INVALID_PERMISSION =
            "Invalid permission.";
}