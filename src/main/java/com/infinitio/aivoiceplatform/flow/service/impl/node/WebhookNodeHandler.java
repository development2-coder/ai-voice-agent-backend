package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler for WEBHOOK nodes.
 *
 * <p>
 * Executes an outbound HTTP request configured by the Flow node
 * and stores the normalized response in the Flow execution context.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 *
 * <pre>
 * {
 *   "url": "https://example.com/api/customer",
 *   "method": "POST",
 *   "headers": {
 *     "Content-Type": "application/json"
 *   },
 *   "body": {
 *     "customerId": "{{customerId}}"
 *   },
 *   "responseVariable": "webhookResponse"
 * }
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookNodeHandler
        implements FlowNodeHandler {

    private static final String URL =
            "url";

    private static final String METHOD =
            "method";

    private static final String HEADERS =
            "headers";

    private static final String BODY =
            "body";

    private static final String RESPONSE_VARIABLE =
            "responseVariable";

    private static final String DEFAULT_RESPONSE_VARIABLE =
            "webhookResponse";

    private static final String ACTION =
            "WEBHOOK";

    private static final int MAX_RESPONSE_LENGTH =
            10000;

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    private final RestClient restClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.WEBHOOK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing WEBHOOK node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        validateContext(
                context
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String url =
                getRequiredString(
                        configuration,
                        URL
                );

        HttpMethod method =
                parseMethod(
                        configuration.get(
                                METHOD
                        )
                );

        Map<String, Object> headers =
                resolveHeaders(
                        configuration.get(
                                HEADERS
                        ),
                        context
                );

        Object body =
                resolveValue(
                        configuration.get(
                                BODY
                        ),
                        context
                );

        String responseVariable =
                getResponseVariable(
                        configuration
                );

        log.info(
                "Sending WEBHOOK request. " +
                        "executionPublicId={}, nodeKey={}, method={}, url={}",
                execution.getPublicId(),
                node.getNodeKey(),
                method,
                url
        );

        ResponseEntity<String> response =
                executeRequest(
                        url,
                        method,
                        headers,
                        body
                );

        String responseBody =
                normalizeResponse(
                        response.getBody()
                );

        Map<String, Object> webhookResult =
                buildResponseContext(
                        response,
                        responseBody
                );

        context.put(
                responseVariable,
                webhookResult
        );

        context.put(
                "lastWebhookResponse",
                webhookResult
        );

        log.info(
                "WEBHOOK request completed. " +
                        "executionPublicId={}, nodeKey={}, status={}, responseVariable={}",
                execution.getPublicId(),
                node.getNodeKey(),
                response.getStatusCode().value(),
                responseVariable
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        ACTION
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .outputText(
                        responseBody
                )
                .build();
    }

    // =========================================================
    // HTTP
    // =========================================================

    private ResponseEntity<String> executeRequest(
            String url,
            HttpMethod method,
            Map<String, Object> headers,
            Object body) {

        try {

            RestClient.RequestBodySpec request =
                    restClient
                            .method(method)
                            .uri(url);

            applyHeaders(
                    request,
                    headers
            );

            if (body == null
                    || method == HttpMethod.GET
                    || method == HttpMethod.HEAD
                    || method == HttpMethod.DELETE) {

                return request
                        .retrieve()
                        .toEntity(
                                String.class
                        );
            }

            return request
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .body(
                            body
                    )
                    .retrieve()
                    .toEntity(
                            String.class
                    );

        } catch (Exception exception) {

            log.error(
                    "WEBHOOK request failed. url={}, method={}, error={}",
                    url,
                    method,
                    exception.getMessage(),
                    exception
            );

            throw new IllegalStateException(
                    FlowMessages.EXECUTION_FAILED,
                    exception
            );
        }
    }

    private void applyHeaders(
            RestClient.RequestHeadersSpec<?> request,
            Map<String, Object> headers) {

        if (headers == null
                || headers.isEmpty()) {

            return;
        }

        headers.forEach(
                (key, value) -> {

                    if (key == null
                            || key.isBlank()
                            || value == null) {

                        return;
                    }

                    request.header(
                            key,
                            String.valueOf(value)
                    );
                }
        );
    }

    // =========================================================
    // CONFIGURATION
    // =========================================================

    private Map<String, Object> readConfiguration(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            log.warn(
                    "WEBHOOK node configuration is empty."
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        try {

            return objectMapper.readValue(
                    configuration,
                    new TypeReference<
                            Map<String, Object>>() {
                    }
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse WEBHOOK configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    private String getRequiredString(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        if (result.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return result;
    }

    private HttpMethod parseMethod(
            Object value) {

        String method =
                value == null
                        ? "GET"
                        : String.valueOf(
                        value
                ).trim();

        if (method.isBlank()) {
            method = "GET";
        }

        try {

            return HttpMethod.valueOf(
                    method.toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            log.warn(
                    "Unsupported WEBHOOK HTTP method. method={}",
                    method
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    private String getResponseVariable(
            Map<String, Object> configuration) {

        Object value =
                configuration.get(
                        RESPONSE_VARIABLE
                );

        if (value == null) {
            return DEFAULT_RESPONSE_VARIABLE;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isBlank()
                ? DEFAULT_RESPONSE_VARIABLE
                : result;
    }

    // =========================================================
    // VALUE RESOLUTION
    // =========================================================

    private Map<String, Object> resolveHeaders(
            Object headersValue,
            Map<String, Object> context) {

        if (!(headersValue instanceof Map<?, ?> headers)) {
            return Map.of();
        }

        Map<String, Object> resolved =
                new LinkedHashMap<>();

        headers.forEach(
                (key, value) ->
                        resolved.put(
                                String.valueOf(key),
                                resolveValue(
                                        value,
                                        context
                                )
                        )
        );

        return resolved;
    }

    private Object resolveValue(
            Object value,
            Map<String, Object> context) {

        if (value == null) {
            return null;
        }

        if (value instanceof String stringValue) {

            return flowContextService.replaceVariables(
                    stringValue,
                    context
            );
        }

        if (value instanceof Map<?, ?> map) {

            Map<String, Object> resolved =
                    new LinkedHashMap<>();

            map.forEach(
                    (key, mapValue) ->
                            resolved.put(
                                    String.valueOf(key),
                                    resolveValue(
                                            mapValue,
                                            context
                                    )
                            )
            );

            return resolved;
        }

        if (value instanceof Iterable<?> iterable) {

            java.util.List<Object> resolved =
                    new java.util.ArrayList<>();

            for (Object item : iterable) {

                resolved.add(
                        resolveValue(
                                item,
                                context
                        )
                );
            }

            return resolved;
        }

        return value;
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private Map<String, Object> buildResponseContext(
            ResponseEntity<String> response,
            String responseBody) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "status",
                response.getStatusCode().value()
        );

        result.put(
                "success",
                response.getStatusCode()
                        .is2xxSuccessful()
        );

        result.put(
                "body",
                responseBody
        );

        return result;
    }

    private String normalizeResponse(
            String responseBody) {

        if (responseBody == null) {
            return "";
        }

        if (responseBody.length()
                <= MAX_RESPONSE_LENGTH) {

            return responseBody;
        }

        log.warn(
                "WEBHOOK response exceeded configured size. " +
                        "length={}, maxLength={}",
                responseBody.length(),
                MAX_RESPONSE_LENGTH
        );

        return responseBody.substring(
                0,
                MAX_RESPONSE_LENGTH
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateContext(
            Map<String, Object> context) {

        if (context != null) {
            return;
        }

        throw new IllegalArgumentException(
                FlowMessages.EXECUTION_FAILED
        );
    }
}