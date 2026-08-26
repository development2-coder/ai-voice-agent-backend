package com.infinitio.aivoiceplatform.exception;

/**
 * Thrown for unexpected server errors.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public class InternalServerException extends BaseException {

    public InternalServerException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }

}