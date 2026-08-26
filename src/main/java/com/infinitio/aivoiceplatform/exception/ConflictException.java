package com.infinitio.aivoiceplatform.exception;

/**
 * Thrown when duplicate/conflicting data exists.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }

}