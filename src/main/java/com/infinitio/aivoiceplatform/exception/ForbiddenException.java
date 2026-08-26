package com.infinitio.aivoiceplatform.exception;

/**
 * Thrown when user doesn't have permission.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

}