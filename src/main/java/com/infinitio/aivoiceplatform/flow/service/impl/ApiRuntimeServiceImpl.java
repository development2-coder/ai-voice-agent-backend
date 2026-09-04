package com.infinitio.aivoiceplatform.flow.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.service.ApiRuntimeService;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link ApiRuntimeService}.
 *
 * <p>
 * Executes HTTP requests configured by Flow API nodes. The service
 * does not contain client-specific URLs, credentials, or API
 * definitions. Those values are supplied by the Flow node
 * configuration at runtime.
 * </p>
 *
 * <p>
 * Java's standard {@link HttpClient} is used so that the Flow module
 * does not require an additional HTTP client dependency.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class ApiRuntimeServiceImpl
        implements ApiRuntimeService {

    /**
     * Default request timeout.
     */
    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(30);

    /**
     * JSON content type.
     */
    private static final String CONTENT_TYPE_JSON =
            "application/json";

    /**
     * Creates the API runtime service.
     *
     * @param objectMapper JSON mapper
     */
    public ApiRuntimeServiceImpl(
            ObjectMapper objectMapper) {

        this.objectMapper =
                objectMapper;

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                REQUEST_TIMEOUT
                        )
                        .build();
    }

    /**
     * JSON mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * HTTP client.
     */
    private final HttpClient httpClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(
            String url,
            String method,
            Map<String, Object> headers,
            Object body) {

        validateUrl(
                url
        );

        String normalizedMethod =
                normalizeMethod(
                        method
                );

        try {

            HttpRequest.BodyPublisher bodyPublisher =
                    buildBodyPublisher(
                            body
                    );

            HttpRequest.Builder requestBuilder =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            url
                                    )
                            )
                            .timeout(
                                    REQUEST_TIMEOUT
                            )
                            .method(
                                    normalizedMethod,
                                    bodyPublisher
                            );

            applyHeaders(
                    requestBuilder,
                    headers
            );

            HttpRequest request =
                    requestBuilder.build();

            log.info(
                    "Executing Flow API request. method={}, url={}",
                    normalizedMethod,
                    url
            );

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            log.debug(
                    "Flow API response received. method={}, url={}, " +
                            "statusCode={}",
                    normalizedMethod,
                    url,
                    response.statusCode()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                log.warn(
                        "Flow API request returned non-success status. " +
                                "method={}, url={}, statusCode={}",
                        normalizedMethod,
                        url,
                        response.statusCode()
                );

                throw new IllegalStateException(
                        FlowMessages.API_REQUEST_FAILED
                                + " HTTP status: "
                                + response.statusCode()
                );
            }

            return parseResponse(
                    response.body()
            );

        } catch (IllegalArgumentException exception) {

            throw exception;

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            log.error(
                    "Flow API request was interrupted. " +
                            "method={}, url={}",
                    normalizedMethod,
                    url,
                    exception
            );

            throw new IllegalStateException(
                    FlowMessages.API_REQUEST_FAILED,
                    exception
            );

        } catch (Exception exception) {

            log.error(
                    "Flow API request failed. method={}, url={}",
                    normalizedMethod,
                    url,
                    exception
            );

            throw new IllegalStateException(
                    FlowMessages.API_REQUEST_FAILED,
                    exception
            );
        }
    }

    /**
     * Validates the API URL.
     *
     * @param url API URL
     */
    private void validateUrl(
            String url) {

        if (url == null
                || url.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.API_URL_REQUIRED
            );
        }

        try {

            URI uri =
                    URI.create(
                            url
                    );

            if (uri.getScheme() == null
                    || uri.getHost() == null) {

                throw new IllegalArgumentException(
                        FlowMessages.API_URL_REQUIRED
                );
            }

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    FlowMessages.API_URL_REQUIRED,
                    exception
            );
        }
    }

    /**
     * Normalizes and validates the HTTP method.
     *
     * @param method HTTP method
     * @return normalized HTTP method
     */
    private String normalizeMethod(
            String method) {

        if (method == null
                || method.isBlank()) {

            return "POST";
        }

        String normalizedMethod =
                method.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (normalizedMethod) {

            case "GET",
                 "POST",
                 "PUT",
                 "PATCH",
                 "DELETE",
                 "HEAD",
                 "OPTIONS" ->
                    normalizedMethod;

            default ->
                    throw new IllegalArgumentException(
                            FlowMessages.API_METHOD_INVALID
                    );
        };
    }

    /**
     * Creates an HTTP request body.
     *
     * @param body configured API body
     * @return body publisher
     */
    private HttpRequest.BodyPublisher buildBodyPublisher(
            Object body) {

        if (body == null) {

            return HttpRequest.BodyPublishers.noBody();
        }

        try {

            if (body instanceof String stringBody) {

                if (stringBody.isBlank()) {

                    return HttpRequest.BodyPublishers.noBody();
                }

                return HttpRequest.BodyPublishers.ofString(
                        stringBody
                );
            }

            return HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                            body
                    )
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to serialize Flow API request body.",
                    exception
            );

            throw new IllegalStateException(
                    FlowMessages.API_REQUEST_FAILED,
                    exception
            );
        }
    }

    /**
     * Applies configured request headers.
     *
     * @param requestBuilder HTTP request builder
     * @param headers configured headers
     */
    private void applyHeaders(
            HttpRequest.Builder requestBuilder,
            Map<String, Object> headers) {

        if (headers == null
                || headers.isEmpty()) {

            return;
        }

        headers.forEach(
                (name, value) -> {

                    if (name == null
                            || name.isBlank()
                            || value == null) {

                        return;
                    }

                    requestBuilder.header(
                            name,
                            String.valueOf(
                                    value
                            )
                    );
                }
        );
    }

    /**
     * Converts a response body to a JSON object when possible.
     *
     * <p>
     * Plain-text responses remain strings. JSON responses are returned
     * as Jackson nodes so that downstream Flow nodes can access
     * structured response data.
     * </p>
     *
     * @param responseBody response body
     * @return parsed response
     */
    private Object parseResponse(
            String responseBody) {

        if (responseBody == null
                || responseBody.isBlank()) {

            return "";
        }

        try {

            JsonNode jsonNode =
                    objectMapper.readTree(
                            responseBody
                    );

            return jsonNode == null
                    ? responseBody
                    : jsonNode;

        } catch (Exception exception) {

            return responseBody;
        }
    }
}