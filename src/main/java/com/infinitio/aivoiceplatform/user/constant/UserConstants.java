package com.infinitio.aivoiceplatform.user.constant;

/**
 * User Constants.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class UserConstants {

    private UserConstants() {
    }

    public static final int USERNAME_MAX_LENGTH = 100;

    public static final int EMAIL_MAX_LENGTH = 150;

    public static final int PASSWORD_MAX_LENGTH = 500;

    public static final int NAME_MAX_LENGTH = 100;

    public static final int FULL_NAME_MAX_LENGTH = 300;

    public static final int MOBILE_MAX_LENGTH = 20;

    public static final int DESIGNATION_MAX_LENGTH = 150;

    public static final int DEPARTMENT_MAX_LENGTH = 150;

    public static final int PROFILE_IMAGE_MAX_LENGTH = 500;


    // =========================================================
    // SYSTEM / BOOTSTRAP
    // =========================================================

    /**
     * System creator ID used only when creating
     * the very first Super Admin.
     *
     * No real user owns this ID.
     */
    public static final Long SYSTEM_USER_ID = 0L;


    /**
     * Role code of the single Super Admin.
     */
    public static final String SUPER_ADMIN_ROLE_CODE =
            "SUPER_ADMIN";


    /**
     * Default page number.
     */
    public static final int DEFAULT_PAGE = 0;


    /**
     * Default page size.
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

}