package com.infinitio.aivoiceplatform.common.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for Request ID handling.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class RequestIdUtil {

    public static final String REQUEST_ID = "requestId";

    private RequestIdUtil() {
    }

    public static String generateRequestId() {

        return UUID.randomUUID().toString();

    }

    public static void setRequestId(String requestId) {

        MDC.put(REQUEST_ID, requestId);

    }

    public static String getRequestId() {

        return MDC.get(REQUEST_ID);

    }

    public static void clear() {

        MDC.remove(REQUEST_ID);

    }

}