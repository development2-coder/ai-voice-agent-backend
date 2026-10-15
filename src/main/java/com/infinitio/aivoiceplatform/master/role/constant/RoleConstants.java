package com.infinitio.aivoiceplatform.master.role.constant;

/**
 * Role Constants.
 *
 * <p>
 * Defines the standard application roles used by the
 * AI Voice Platform.
 * </p>
 *
 * <p>
 * Role hierarchy:
 * </p>
 *
 * <pre>
 * SUPER_ADMIN
 *      |
 *      +-- ADMIN
 *              |
 *              +-- USER
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class RoleConstants {

    private RoleConstants() {
    }

    /**
     * Super administrator.
     *
     * <p>
     * Represents the company/platform owner and has
     * global access across the application.
     * </p>
     */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /**
     * Administrator.
     *
     * <p>
     * Represents a client/admin and is restricted to
     * its own tenant.
     * </p>
     */
    public static final String ADMIN = "ADMIN";

    /**
     * Normal application user.
     *
     * <p>
     * A USER can access only the permissions assigned
     * to the user's role within the user's tenant.
     * </p>
     */
    public static final String USER = "USER";

    /*
     * Kept for backward compatibility with existing
     * database/application data.
     *
     * These roles are not part of the new standard
     * SUPER_ADMIN -> ADMIN -> USER hierarchy.
     */
    public static final String SUPERVISOR = "SUPERVISOR";

    public static final String AGENT = "AGENT";
}