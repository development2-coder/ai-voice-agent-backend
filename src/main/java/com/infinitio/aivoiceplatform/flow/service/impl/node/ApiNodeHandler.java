package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiNodeHandler
        implements FlowNodeHandler {

    private static final String URL_KEY = "url";

    private static final String METHOD_KEY = "method";

    private static final String BODY_KEY = "body";

    private static final String HEADERS_KEY = "headers";

    private static final String RESPONSE_VARIABLE_KEY =
            "responseVariable";

    private static final String API_REQUEST_KEY =
            "_apiRequest";

    private static final String WAITING_API_VARIABLE_KEY =
            "_waitingApiVariable";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.API;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing API node. execution={}, node={}",
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

            method = "POST";
        }

        /*
         * Resolve variables in URL.
         */
        url =
                flowContextService.replaceVariables(
                        url,
                        context
                );

        /*
         * Resolve variables in request body.
         */
        Object body =
                configuration.get(
                        BODY_KEY
                );

        Object resolvedBody =
                resolveVariables(
                        body,
                        context
                );

        /*
         * Resolve variables in headers.
         */
        Object headers =
                configuration.get(
                        HEADERS_KEY
                );

        Object resolvedHeaders =
                resolveVariables(
                        headers,
                        context
                );

        String responseVariable =
                getOptionalValue(
                        configuration,
                        RESPONSE_VARIABLE_KEY
                );

        /*
         * Prepare generic API request.
         *
         * The actual HTTP request will be handled
         * by the integration/API layer.
         */
        Map<String, Object> apiRequest =
                new HashMap<>();

        apiRequest.put(
                "url",
                url
        );

        apiRequest.put(
                "method",
                method.toUpperCase()
        );

        apiRequest.put(
                "headers",
                resolvedHeaders
        );

        apiRequest.put(
                "body",
                resolvedBody
        );

        if (responseVariable != null
                && !responseVariable.isBlank()) {

            apiRequest.put(
                    "responseVariable",
                    responseVariable
            );

            context.put(
                    WAITING_API_VARIABLE_KEY,
                    responseVariable
            );
        }

        context.put(
                API_REQUEST_KEY,
                apiRequest
        );

        log.debug(
                "API request prepared. execution={}, method={}, url={}",
                execution.getPublicId(),
                method,
                url
        );

        /*
         * API execution is asynchronous from the
         * Flow Engine's point of view.
         */
        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.WAITING_FOR_API
                )
                .action(
                        "EXECUTE_API"
                )
                .waiting(true)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    private Map<String, Object> readConfiguration(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            return new HashMap<>();
        }

        try {

            return objectMapper.readValue(
                    configuration,
                    Map.class
            );

        } catch (Exception exception) {

            log.error(
                    "Invalid API node configuration. configuration={}",
                    configuration,
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid API node configuration.",
                    exception
            );
        }
    }

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

            throw new IllegalArgumentException(
                    "API node requires: " + key
            );
        }

        return value;
    }

    private String getOptionalValue(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(key);

        return value == null
                ? null
                : String.valueOf(value).trim();
    }

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
                                    String.valueOf(key),
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
}