package com.infinitio.aivoiceplatform.master.rolemenu.constant;

/**
 * Role Menu Messages.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class RoleMenuMessages {

    private RoleMenuMessages() {
    }


    // =========================================================
    // SUCCESS MESSAGES
    // =========================================================

    public static final String CREATED =
            "Role menu mapping created successfully.";

    public static final String UPDATED =
            "Role menu mapping updated successfully.";

    public static final String DELETED =
            "Role menu mapping deleted successfully.";

    public static final String ACTIVATED =
            "Role menu mapping activated successfully.";

    public static final String DEACTIVATED =
            "Role menu mapping deactivated successfully.";

    public static final String FETCHED =
            "Role menu mapping fetched successfully.";

    public static final String FETCHED_ALL =
            "Role menu mappings fetched successfully.";


    // =========================================================
    // ERROR MESSAGES
    // =========================================================

    public static final String NOT_FOUND =
            "Role menu mapping not found.";

    public static final String ALREADY_EXISTS =
            "Role and menu mapping already exists.";

    public static final String INVALID_ROLE =
            "Invalid role.";

    public static final String INVALID_MENU =
            "Invalid menu.";

    public static final String INVALID_VISIBILITY =
            "Visibility must be either 0 or 1.";
}