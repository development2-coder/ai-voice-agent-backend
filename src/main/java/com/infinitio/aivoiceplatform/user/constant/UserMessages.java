package com.infinitio.aivoiceplatform.user.constant;

/**
 * User Messages.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class UserMessages {

    private UserMessages() {
    }

    public static final String USER_CREATED =
            "User created successfully.";

    public static final String USER_UPDATED =
            "User updated successfully.";

    public static final String USER_DELETED =
            "User deleted successfully.";

    public static final String USER_NOT_FOUND =
            "User not found.";

    public static final String USERNAME_ALREADY_EXISTS =
            "Username already exists.";

    public static final String EMAIL_ALREADY_EXISTS =
            "Email already exists.";

    public static final String SUPER_ADMIN_ALREADY_EXISTS =
            "Only one SUPER_ADMIN is allowed.";

    public static final String FIRST_USER_MUST_BE_SUPER_ADMIN =
            "The first user must be a SUPER_ADMIN.";

    public static final String AUTHENTICATION_REQUIRED =
            "Authentication is required to create users.";

}