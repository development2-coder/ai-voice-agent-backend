package com.infinitio.aivoiceplatform.exception;

/**
 * Thrown when request is invalid.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

}