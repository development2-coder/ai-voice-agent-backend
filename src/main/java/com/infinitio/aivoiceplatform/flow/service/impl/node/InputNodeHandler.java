package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for USER_INPUT Flow nodes.
 *
 * <p>
 * The node pauses Flow execution until caller input is received.
 * In a voice conversation, the actual audio transcription is
 * performed by the streaming STT runtime. Once the transcript
 * is available, the Flow execution is continued with the
 * configured input variable.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InputNodeHandler
        implements FlowNodeHandler {

    private static final String MESSAGE =
            "message";

    private static final String VARIABLE =
            "variable";

    private static final String VARIABLE_NAME =
            "variableName";

    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.USER_INPUT;
    }

    /**
     * Executes the USER_INPUT node.
     *
     * @param execution current Flow execution
     * @param node current Flow node
     * @param context current Flow context
     * @return waiting Flow execution result
     */
    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        validateExecution(
                execution
        );

        validateNode(
                node
        );

        validateContext(
                context
        );

        log.info(
                "Executing USER_INPUT node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String message =
                getStringValue(
                        configuration,
                        MESSAGE
                );

        /*
         * Support both the existing backend property
         * and the frontend schema property.
         *
         * This keeps the request contract backward compatible.
         */
        String variable =
                getStringValue(
                        configuration,
                        VARIABLE
                );

        if (variable == null
                || variable.isBlank()) {

            variable =
                    getStringValue(
                            configuration,
                            VARIABLE_NAME
                    );
        }

        if (variable == null
                || variable.isBlank()) {

            log.warn(
                    "USER_INPUT variable is missing. " +
                            "executionPublicId={}, nodeKey={}",
                    execution.getPublicId(),
                    node.getNodeKey()
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        /*
         * Store the variable used by the continuation service.
         */
        context.put(
                FlowExecutionContextKeys.WAITING_VARIABLE,
                variable
        );

        log.info(
                "USER_INPUT node is waiting for caller input. " +
                        "executionPublicId={}, nodeKey={}, variable={}",
                execution.getPublicId(),
                node.getNodeKey(),
                variable
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.WAITING_FOR_INPUT
                )
                .action(
                        "WAIT_FOR_INPUT"
                )
                .outputText(
                        message
                )
                .waiting(true)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    /**
     * Reads node configuration.
     *
     * @param configurationJson configuration JSON
     * @return parsed configuration
     */
    private Map<String, Object> readConfiguration(
            String configurationJson) {

        if (configurationJson == null
                || configurationJson.isBlank()) {

            return new HashMap<>();
        }

        try {

            return objectMapper.readValue(
                    configurationJson,
                    Map.class
            );

        } catch (Exception exception) {

            log.error(
                    "Invalid USER_INPUT node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Reads a string configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return value or null
     */
    private String getStringValue(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {
            return null;
        }

        return String.valueOf(
                value
        ).trim();
    }

    /**
     * Validates execution.
     *
     * @param execution current execution
     */
    private void validateExecution(
            FlowExecution execution) {

        if (execution == null) {

            throw new IllegalArgumentException(
                    "Flow execution cannot be null."
            );
        }
    }

    /**
     * Validates node.
     *
     * @param node current node
     */
    private void validateNode(
            FlowNode node) {

        if (node == null) {

            throw new IllegalArgumentException(
                    "Flow node cannot be null."
            );
        }
    }

    /**
     * Validates context.
     *
     * @param context execution context
     */
    private void validateContext(
            Map<String, Object> context) {

        if (context == null) {

            throw new IllegalArgumentException(
                    "Flow context cannot be null."
            );
        }
    }
}