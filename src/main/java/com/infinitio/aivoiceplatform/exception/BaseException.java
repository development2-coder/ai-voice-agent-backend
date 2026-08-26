package com.infinitio.aivoiceplatform.exception;

import lombok.Getter;

/**
 * Base Exception for all custom exceptions.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;

    protected BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}