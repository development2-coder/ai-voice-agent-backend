package com.infinitio.aivoiceplatform.common.util;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.enums.ApiStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class to build standardized API responses.
 *
 * All controllers should use this class instead of creating
 * ResponseEntity manually.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
public final class ResponseBuilder {

    private ResponseBuilder() {
    }

    /**
     * HTTP 200 OK
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {

        log.debug("Building HTTP 200 Success Response.");

        return buildResponse(
                HttpStatus.OK,
                ApiStatus.SUCCESS,
                message,
                data
        );
    }

    /**
     * HTTP 201 CREATED
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {

        log.debug("Building HTTP 201 Created Response.");

        return buildResponse(
                HttpStatus.CREATED,
                ApiStatus.SUCCESS,
                message,
                data
        );
    }

    /**
     * HTTP 202 ACCEPTED
     */
    public static <T> ResponseEntity<ApiResponse<T>> accepted(T data, String message) {

        log.debug("Building HTTP 202 Accepted Response.");

        return buildResponse(
                HttpStatus.ACCEPTED,
                ApiStatus.SUCCESS,
                message,
                data
        );
    }

    /**
     * HTTP 204 NO CONTENT
     */
    public static ResponseEntity<Void> noContent() {

        log.debug("Building HTTP 204 No Content Response.");

        return ResponseEntity.noContent().build();
    }

    /**
     * Generic Response Builder
     */
    private static <T> ResponseEntity<ApiResponse<T>> buildResponse(
            HttpStatus httpStatus,
            ApiStatus apiStatus,
            String message,
            T data) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .requestId(RequestIdUtil.getRequestId())
                .status(apiStatus)
                .statusCode(httpStatus.value())
                .message(message)
                .data(data)
                .build();

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

}