package com.infinitio.aivoiceplatform.flow.service.impl.node;

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

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for SET_VARIABLE nodes.
 *
 * <p>
 * The node creates or updates a variable in the current Flow
 * execution context.
 * </p>
 *
 * <p>
 * Configuration:
 * </p>
 *
 * <pre>
 * {
 *   "variableName": "customerName",
 *   "value": "{{lastUserInput}}"
 * }
 * </pre>
 *
 * <p>
 * This follows the n8n-style concept of a node modifying the
 * workflow data before passing execution to the next node.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetVariableNodeHandler
        implements FlowNodeHandler {

    /**
     * Configuration field containing the variable name.
     */
    private static final String VARIABLE_NAME_KEY =
            "variableName";

    /**
     * Configuration field containing the variable value.
     */
    private static final String VALUE_KEY =
            "value";

    /**
     * Action returned to the Flow runtime.
     */
    private static final String ACTION =
            "SET_VARIABLE";

    /**
     * Object mapper used to parse node configuration.
     */
    private final ObjectMapper objectMapper;

    /**
     * Flow context service used for expression replacement.
     */
    private final FlowContextService flowContextService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.SET_VARIABLE;
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
                "Executing SET_VARIABLE node. " +
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

        String variableName =
                getRequiredValue(
                        configuration,
                        VARIABLE_NAME_KEY
                );

        Object configuredValue =
                configuration.get(
                        VALUE_KEY
                );

        if (configuredValue == null) {

            log.warn(
                    "SET_VARIABLE node value is missing. " +
                            "executionPublicId={}, nodeKey={}",
                    execution.getPublicId(),
                    node.getNodeKey()
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        Object resolvedValue =
                resolveValue(
                        configuredValue,
                        context
                );

        context.put(
                variableName,
                resolvedValue
        );

        log.info(
                "Flow variable updated successfully. " +
                        "executionPublicId={}, nodeKey={}, variable={}",
                execution.getPublicId(),
                node.getNodeKey(),
                variableName
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
                .build();
    }

    // =========================================================
    // CONFIGURATION
    // =========================================================

    /**
     * Parses the node JSON configuration.
     *
     * @param configuration JSON configuration
     * @return parsed configuration
     */
    private Map<String, Object> readConfiguration(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            log.warn(
                    "SET_VARIABLE node configuration is empty."
            );

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
                    "Unable to parse SET_VARIABLE configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Reads a required configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return value
     */
    private String getRequiredValue(
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

        String stringValue =
                String.valueOf(
                        value
                ).trim();

        if (stringValue.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return stringValue;
    }

    // =========================================================
    // VALUE RESOLUTION
    // =========================================================

    /**
     * Resolves expressions in the configured value.
     *
     * <p>
     * String values support the same Flow context expressions
     * already used by the existing AI, RAG and API handlers.
     * </p>
     *
     * <p>
     * Example:
     * </p>
     *
     * <pre>
     * value = "{{lastUserInput}}"
     * </pre>
     *
     * becomes the corresponding value from the execution context.
     *
     * @param value configured value
     * @param context execution context
     * @return resolved value
     */
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
                    new HashMap<>();

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

            return resolveIterable(
                    iterable,
                    context
            );
        }

        return value;
    }

    /**
     * Resolves expression values inside a collection.
     *
     * @param iterable source collection
     * @param context execution context
     * @return resolved collection
     */
    private Object resolveIterable(
            Iterable<?> iterable,
            Map<String, Object> context) {

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

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Validates the execution context.
     *
     * @param context execution context
     */
    private void validateContext(
            Map<String, Object> context) {

        if (context != null) {
            return;
        }

        log.error(
                "SET_VARIABLE execution context is null."
        );

        throw new IllegalArgumentException(
                FlowMessages.EXECUTION_FAILED
        );
    }
}