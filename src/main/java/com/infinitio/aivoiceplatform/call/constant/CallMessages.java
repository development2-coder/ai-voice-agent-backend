package com.infinitio.aivoiceplatform.call.constant;

/**
 * Messages used by Call module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class CallMessages {

    private CallMessages() {
    }

    public static final String CREATED =
            "Call created successfully.";

    public static final String UPDATED =
            "Call updated successfully.";

    public static final String DELETED =
            "Call deleted successfully.";

    public static final String NOT_FOUND =
            "Call not found.";

    public static final String PROVIDER_CALL_ID_ALREADY_EXISTS =
            "Provider call ID already exists.";

    public static final String FETCHED =
            "Call fetched successfully.";

    public static final String FETCHED_ALL =
            "Calls fetched successfully.";

    public static final String FETCHED_BY_TENANT =
            "Tenant calls fetched successfully.";

    public static final String SUPER_ADMIN_REQUIRED =
            "Only SUPER_ADMIN can access calls by tenant.";

    public static final String INVALID_TENANT_ID =
            "Tenant ID must be greater than zero.";
}