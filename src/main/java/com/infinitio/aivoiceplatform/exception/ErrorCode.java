package com.infinitio.aivoiceplatform.exception;

/**
 * Application Error Codes.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    // Common Errors
    public static final String BAD_REQUEST = "COM-400";
    public static final String UNAUTHORIZED = "COM-401";
    public static final String FORBIDDEN = "COM-403";
    public static final String RESOURCE_NOT_FOUND = "COM-404";
    public static final String METHOD_NOT_ALLOWED = "COM-405";
    public static final String CONFLICT = "COM-409";
    public static final String VALIDATION_ERROR = "COM-422";
    public static final String INTERNAL_SERVER_ERROR = "COM-500";

}