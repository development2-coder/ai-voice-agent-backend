package com.infinitio.aivoiceplatform.master.usermenu.constant;

/**
 * User Menu Messages.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class UserMenuMessages {

    private UserMenuMessages() {
    }

    public static final String CREATED =
            "User menu created successfully.";

    public static final String UPDATED =
            "User menu updated successfully.";

    public static final String FETCHED =
            "User menu fetched successfully.";

    public static final String FETCHED_ALL =
            "User menus fetched successfully.";

    public static final String DELETED =
            "User menu deleted successfully.";

    public static final String ACTIVATED =
            "User menu activated successfully.";

    public static final String DEACTIVATED =
            "User menu deactivated successfully.";

    public static final String NOT_FOUND =
            "User menu not found.";

    public static final String ALREADY_EXISTS =
            "User menu already exists.";

    public static final String INVALID_USER =
            "Invalid user.";

    public static final String INVALID_MENU =
            "Invalid menu.";

    public static final String INVALID_VISIBILITY =
            "Visibility must be either 0 or 1.";

    public static final String USER_OUTSIDE_TENANT =
            "You cannot manage a user outside your tenant.";

    public static final String MENU_NOT_AVAILABLE =
            "You cannot assign a menu that you do not have access to.";

    public static final String USER_MENU_ONLY_FOR_USER =
            "User menu access can only be assigned to USER accounts.";

    public static final String ADMIN_ONLY =
            "Only ADMIN or SUPER_ADMIN can manage user menu access.";
}