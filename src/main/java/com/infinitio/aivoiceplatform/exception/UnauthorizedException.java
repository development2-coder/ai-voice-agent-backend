package com.infinitio.aivoiceplatform.exception;

/**
 * Thrown when user is not authenticated.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

}