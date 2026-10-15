package com.infinitio.aivoiceplatform.common.constants;

/**
 * Common application-level constants used across
 * the AI Voice Platform.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        // Utility class.
    }

    /**
     * Active record flag.
     */
    public static final Integer ACTIVE = 1;

    /**
     * Inactive record flag.
     */
    public static final Integer INACTIVE = 0;

    /**
     * Record is not deleted.
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * Record is deleted.
     */
    public static final Integer DELETED = 1;
}