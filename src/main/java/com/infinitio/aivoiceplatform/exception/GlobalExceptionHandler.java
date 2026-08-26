package com.infinitio.aivoiceplatform.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.infinitio.aivoiceplatform.common.dto.ErrorResponse;
import com.infinitio.aivoiceplatform.common.enums.ApiStatus;
import com.infinitio.aivoiceplatform.common.util.RequestIdUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles application-wide exceptions and converts them into
 * standardized error responses.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all custom application exceptions.
     *
     * @param exception custom application exception
     * @param request HTTP request
     * @return standardized error response with appropriate HTTP status
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException exception,
            HttpServletRequest request) {

        HttpStatus status =
                resolveHttpStatus(exception.getErrorCode());

        log.error(
                "Business exception occurred. errorCode={}, status={}, message={}, path={}",
                exception.getErrorCode(),
                status.value(),
                exception.getMessage(),
                request.getRequestURI(),
                exception
        );

        ErrorResponse response =
                buildErrorResponse(
                        status,
                        exception.getErrorCode(),
                        exception.getMessage(),
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Handles Bean Validation failures.
     *
     * @param exception validation exception
     * @param request HTTP request
     * @return validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<String> validationErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();

        log.warn(
                "Validation failed. path={}, errors={}",
                request.getRequestURI(),
                validationErrors
        );

        ErrorResponse response =
                buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.VALIDATION_ERROR,
                        "Validation Failed.",
                        request.getRequestURI(),
                        validationErrors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles constraint validation failures.
     *
     * @param exception constraint violation exception
     * @param request HTTP request
     * @return validation error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        List<String> errors =
                exception.getConstraintViolations()
                        .stream()
                        .map(violation -> violation.getMessage())
                        .toList();

        log.warn(
                "Constraint validation failed. path={}, errors={}",
                request.getRequestURI(),
                errors
        );

        ErrorResponse response =
                buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.VALIDATION_ERROR,
                        "Validation Failed.",
                        request.getRequestURI(),
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles malformed JSON requests.
     *
     * @param exception JSON parsing exception
     * @param request HTTP request
     * @return bad request response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        log.warn(
                "Malformed JSON request. path={}, message={}",
                request.getRequestURI(),
                exception.getMessage()
        );

        ErrorResponse response =
                buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST,
                        "Malformed JSON Request.",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles database integrity violations.
     *
     * @param exception database integrity exception
     * @param request HTTP request
     * @return conflict error response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        log.error(
                "Database integrity violation. path={}",
                request.getRequestURI(),
                exception
        );

        ErrorResponse response =
                buildErrorResponse(
                        HttpStatus.CONFLICT,
                        ErrorCode.CONFLICT,
                        "Duplicate record found.",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    /**
     * Handles unsupported HTTP methods.
     *
     * @param exception HTTP method exception
     * @param request HTTP request
     * @return method not allowed response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        log.warn(
                "HTTP method not allowed. method={}, path={}",
                request.getMethod(),
                request.getRequestURI()
        );

        ErrorResponse response =
                buildErrorResponse(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        ErrorCode.METHOD_NOT_ALLOWED,
                        exception.getMessage(),
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }

    /**
     * Handles unsupported media types.
     *
     * @param exception media type exception
     * @param request HTTP request
     * @return unsupported media type response
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {

        log.warn(
                "Unsupported media type. path={}, message={}",
                request.getRequestURI(),
                exception.getMessage()
        );

        ErrorResponse response =
                buildErrorResponse(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "COM-415",
                        exception.getMessage(),
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(response);
    }

    /**
     * Handles unexpected exceptions.
     *
     * @param exception unexpected exception
     * @param request HTTP request
     * @return internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request) {

        log.error(
                "Unexpected exception occurred. path={}",
                request.getRequestURI(),
                exception
        );

        ErrorResponse response =
                buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "Something went wrong.",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Resolves HTTP status from the application error code.
     *
     * @param errorCode application error code
     * @return corresponding HTTP status
     */
    private HttpStatus resolveHttpStatus(
            String errorCode) {

        if (ErrorCode.BAD_REQUEST.equals(errorCode)) {
            return HttpStatus.BAD_REQUEST;
        }

        if (ErrorCode.UNAUTHORIZED.equals(errorCode)) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (ErrorCode.FORBIDDEN.equals(errorCode)) {
            return HttpStatus.FORBIDDEN;
        }

        if (ErrorCode.RESOURCE_NOT_FOUND.equals(errorCode)) {
            return HttpStatus.NOT_FOUND;
        }

        if (ErrorCode.METHOD_NOT_ALLOWED.equals(errorCode)) {
            return HttpStatus.METHOD_NOT_ALLOWED;
        }

        if (ErrorCode.CONFLICT.equals(errorCode)) {
            return HttpStatus.CONFLICT;
        }

        if (ErrorCode.VALIDATION_ERROR.equals(errorCode)) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }

        if (ErrorCode.INTERNAL_SERVER_ERROR.equals(errorCode)) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Builds the standardized error response.
     *
     * @param status HTTP status
     * @param errorCode application error code
     * @param message error message
     * @param path request path
     * @param validationErrors validation errors
     * @return standardized error response
     */
    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            String path,
            List<String> validationErrors) {

        return ErrorResponse.builder()
                .requestId(RequestIdUtil.getRequestId())
                .status(ApiStatus.ERROR)
                .statusCode(status.value())
                .error(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();
    }
}