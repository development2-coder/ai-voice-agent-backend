package com.infinitio.aivoiceplatform.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infinitio.aivoiceplatform.common.enums.ApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic Error Response.
 *
 * Used by GlobalExceptionHandler.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Request Identifier.
     */
    private String requestId;

    /**
     * SUCCESS / FAILED / ERROR
     */
    private ApiStatus status;

    /**
     * HTTP Status Code.
     */
    private int statusCode;

    /**
     * Error Name.
     */
    private String error;

    /**
     * Error Message.
     */
    private String message;

    /**
     * API Path.
     */
    private String path;

    /**
     * Error Time.
     */
    private LocalDateTime timestamp;

    /**
     * Validation Errors.
     */
    private List<String> validationErrors;

}