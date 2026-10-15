package com.infinitio.aivoiceplatform.master.adminmenu.constant;

/**
 * Admin Menu messages.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class AdminMenuMessages {

    private AdminMenuMessages() {
    }

    public static final String CREATED =
            "Admin menu created successfully.";

    public static final String UPDATED =
            "Admin menu updated successfully.";

    public static final String FETCHED =
            "Admin menu fetched successfully.";

    public static final String FETCHED_ALL =
            "Admin menus fetched successfully.";

    public static final String DELETED =
            "Admin menu deleted successfully.";

    public static final String ACTIVATED =
            "Admin menu activated successfully.";

    public static final String DEACTIVATED =
            "Admin menu deactivated successfully.";

    public static final String NOT_FOUND =
            "Admin menu not found.";

    public static final String ALREADY_EXISTS =
            "Menu is already assigned to this ADMIN.";

    public static final String ADMIN_NOT_FOUND =
            "ADMIN/client was not found.";

    public static final String MENU_NOT_FOUND =
            "Menu was not found.";

    public static final String ONLY_ADMIN_TENANT =
            "Menu can only be assigned to an ADMIN/client.";

    public static final String INVALID_VISIBILITY =
            "Visibility must be either 0 or 1.";

    public static final String SUPER_ADMIN_ONLY =
            "Only SUPER_ADMIN can manage ADMIN menu access.";
}