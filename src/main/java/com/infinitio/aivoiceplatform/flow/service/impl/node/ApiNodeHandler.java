package com.infinitio.aivoiceplatform.flow.service.impl.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.ApiRuntimeService;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;

import lombok.extern.slf4j.Slf4j;

/**
 * Flow node handler for API execution.
 *
 * <p>
 * The handler reads the API configuration defined by the client,
 * resolves Flow variables, delegates HTTP execution to
 * {@link ApiRuntimeService}, and stores the response in the Flow
 * context.
 * </p>
 *
 * <p>
 * The handler does not determine which node executes next. The
 * Flow runtime remains responsible for following the client's
 * configured graph.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
public class ApiNodeHandler
        implements FlowNodeHandler {

    /**
     * API configuration URL key.
     */
    private static final String URL_KEY =
            "url";

    /**
     * API configuration HTTP method key.
     */
    private static final String METHOD_KEY =
            "method";

    /**
     * API configuration body key.
     */
    private static final String BODY_KEY =
            "body";

    /**
     * API configuration headers key.
     */
    private static final String HEADERS_KEY =
            "headers";

    /**
     * API configuration response variable key.
     */
    private static final String RESPONSE_VARIABLE_KEY =
            "responseVariable";

    /**
     * Default HTTP method.
     */
    private static final String DEFAULT_METHOD =
            "POST";

    /**
     * Last API response context variable.
     */
    private static final String LAST_API_RESPONSE =
            "lastApiResponse";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    private final ApiRuntimeService apiRuntimeService;

    /**
     * Creates the API node handler.
     *
     * @param objectMapper JSON mapper
     * @param flowContextService Flow context service
     * @param apiRuntimeService API runtime service
     */
    public ApiNodeHandler(
            ObjectMapper objectMapper,
            FlowContextService flowContextService,
            ApiRuntimeService apiRuntimeService) {

        this.objectMapper =
                objectMapper;

        this.flowContextService =
                flowContextService;

        this.apiRuntimeService =
                apiRuntimeService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.API;
    }

    /**
     * Executes the configured API node.
     *
     * @param execution current Flow execution
     * @param node current Flow node
     * @param context current Flow context
     * @return API execution result
     */
    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        validateArguments(
                execution,
                node,
                context
        );

        log.info(
                "Executing API Flow node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String url =
                getRequiredValue(
                        configuration,
                        URL_KEY
                );

        String method =
                getOptionalValue(
                        configuration,
                        METHOD_KEY
                );

        if (method == null
                || method.isBlank()) {

            method =
                    DEFAULT_METHOD;
        }

        url =
                flowContextService.replaceVariables(
                        url,
                        context
                );

        Object body =
                resolveVariables(
                        configuration.get(
                                BODY_KEY
                        ),
                        context
                );

        Map<String, Object> headers =
                resolveHeaders(
                        configuration.get(
                                HEADERS_KEY
                        ),
                        context
                );

        String responseVariable =
                getOptionalValue(
                        configuration,
                        RESPONSE_VARIABLE_KEY
                );

        if (responseVariable != null
                && responseVariable.isBlank()) {

            responseVariable =
                    null;
        }

        log.debug(
                "Executing configured API request. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "method={}, url={}, responseVariable={}",
                execution.getPublicId(),
                node.getNodeKey(),
                method,
                url,
                responseVariable
        );

        Object response =
                apiRuntimeService.execute(
                        url,
                        method,
                        headers,
                        body
                );

        context.put(
                LAST_API_RESPONSE,
                response
        );

        if (responseVariable != null) {

            validateResponseVariable(
                    responseVariable
            );

            flowContextService.setVariable(
                    context,
                    responseVariable,
                    response
            );
        }

        context.remove(
                FlowExecutionContextKeys.API_REQUEST
        );

        context.remove(
                FlowExecutionContextKeys.WAITING_API_VARIABLE
        );

        log.info(
                "API Flow node completed successfully. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        "API"
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    /**
     * Reads JSON node configuration.
     *
     * @param configuration JSON configuration
     * @return configuration map
     */
    private Map<String, Object> readConfiguration(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        try {

            return objectMapper.readValue(
                    configuration,
                    Map.class
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse API node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Returns a required configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return configuration value
     */
    private String getRequiredValue(
            Map<String, Object> configuration,
            String key) {

        String value =
                getOptionalValue(
                        configuration,
                        key
                );

        if (value == null
                || value.isBlank()) {

            if (URL_KEY.equals(key)) {

                throw new IllegalArgumentException(
                        FlowMessages.API_URL_REQUIRED
                );
            }

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return value;
    }

    /**
     * Returns an optional configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return configuration value
     */
    private String getOptionalValue(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(
                        key
                );

        return value == null
                ? null
                : String.valueOf(
                value
        ).trim();
    }

    /**
     * Resolves Flow variables recursively.
     *
     * @param value configured value
     * @param context Flow execution context
     * @return resolved value
     */
    private Object resolveVariables(
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
                    new HashMap<>();

            map.forEach(
                    (key, mapValue) ->
                            resolved.put(
                                    String.valueOf(
                                            key
                                    ),
                                    resolveVariables(
                                            mapValue,
                                            context
                                    )
                            )
            );

            return resolved;
        }

        if (value instanceof Iterable<?> iterable) {

            List<Object> resolved =
                    new ArrayList<>();

            for (Object item : iterable) {

                resolved.add(
                        resolveVariables(
                                item,
                                context
                        )
                );
            }

            return resolved;
        }

        return value;
    }

    /**
     * Resolves configured API headers.
     *
     * @param value configured headers
     * @param context Flow context
     * @return resolved headers
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveHeaders(
            Object value,
            Map<String, Object> context) {

        if (value == null) {

            return new HashMap<>();
        }

        Object resolved =
                resolveVariables(
                        value,
                        context
                );

        if (!(resolved instanceof Map<?, ?> map)) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        Map<String, Object> headers =
                new HashMap<>();

        map.forEach(
                (key, headerValue) ->
                        headers.put(
                                String.valueOf(
                                        key
                                ),
                                headerValue
                        )
        );

        return headers;
    }

    /**
     * Validates the response variable name.
     *
     * @param responseVariable response variable
     */
    private void validateResponseVariable(
            String responseVariable) {

        if (responseVariable == null
                || responseVariable.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.API_RESPONSE_VARIABLE_INVALID
            );
        }
    }

    /**
     * Validates handler arguments.
     *
     * @param execution Flow execution
     * @param node Flow node
     * @param context Flow context
     */
    private void validateArguments(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        if (execution == null
                || node == null
                || context == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }
    }
}