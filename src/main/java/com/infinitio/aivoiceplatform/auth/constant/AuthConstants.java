package com.infinitio.aivoiceplatform.auth.constant;

/**
 * Authentication Constants.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class AuthConstants {

    private AuthConstants() {
    }

    /**
     * Token Type
     */
    public static final String TOKEN_TYPE = "Bearer";

    /**
     * Header Names
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Login Types
     */
    public static final String LOGIN_TYPE_WEB = "WEB";

    public static final String LOGIN_TYPE_API = "API";

    public static final String LOGIN_TYPE_MOBILE = "MOBILE";

    /**
     * Device Types
     */
    public static final String DEVICE_DESKTOP = "DESKTOP";

    public static final String DEVICE_MOBILE = "MOBILE";

    public static final String DEVICE_TABLET = "TABLET";

}