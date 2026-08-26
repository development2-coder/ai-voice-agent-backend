package com.infinitio.aivoiceplatform.exception;

/**
 * Thrown when requested resource is not found.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

}