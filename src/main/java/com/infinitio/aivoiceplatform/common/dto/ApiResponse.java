package com.infinitio.aivoiceplatform.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infinitio.aivoiceplatform.common.enums.ApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API Response.
 *
 * All successful API responses should use this class.
 *
 * @param <T> Response Payload
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Unique Request Identifier.
     */
    private String requestId;

    /**
     * SUCCESS / FAILED / ERROR / WARNING
     */
    private ApiStatus status;

    /**
     * HTTP Status Code.
     */
    private int statusCode;

    /**
     * Response Message.
     */
    private String message;

    /**
     * Actual Response Data.
     */
    private T data;

    /**
     * Success Response.
     */
    public static <T> ApiResponse<T> success(
            String message,
            T data) {

        return ApiResponse.<T>builder()
                .status(ApiStatus.SUCCESS)
                .statusCode(200)
                .message(message)
                .data(data)
                .build();

    }

    /**
     * Error Response.
     */
    public static <T> ApiResponse<T> error(
            int statusCode,
            String message) {

        return ApiResponse.<T>builder()
                .status(ApiStatus.ERROR)
                .statusCode(statusCode)
                .message(message)
                .build();

    }

}