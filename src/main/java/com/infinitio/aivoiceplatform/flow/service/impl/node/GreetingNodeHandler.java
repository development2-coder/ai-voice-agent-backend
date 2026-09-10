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
 * Handler for GREETING Flow nodes.
 *
 * <p>
 * The greeting text is configured by the Flow Builder and is
 * passed to the downstream TTS node through the Flow context.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GreetingNodeHandler
        implements FlowNodeHandler {

    private static final String MESSAGE =
            "message";

    private static final String TTS_TEXT =
            "ttsText";

    private static final String ACTION =
            "SPEAK";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.GREETING;
    }

    /**
     * Executes the GREETING node.
     *
     * <p>
     * The configured message is resolved against the current
     * Flow context and stored as {@code ttsText} so that a
     * downstream TTS node can synthesize it.
     * </p>
     *
     * @param execution current Flow execution
     * @param node current Flow node
     * @param context current Flow context
     * @return Flow node execution result
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
                "Executing GREETING node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String greeting =
                getRequiredMessage(
                        configuration
                );

        String resolvedGreeting =
                flowContextService.replaceVariables(
                        greeting,
                        context
                );

        /*
         * Store the generated speech text in Flow context.
         *
         * The downstream TTS node resolves text from this key
         * when no explicit text is configured on the TTS node.
         */
        context.put(
                TTS_TEXT,
                resolvedGreeting
        );

        log.debug(
                "GREETING text prepared for TTS. " +
                        "executionPublicId={}, nodeKey={}, textLength={}",
                execution.getPublicId(),
                node.getNodeKey(),
                resolvedGreeting.length()
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        ACTION
                )
                .outputText(
                        resolvedGreeting
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    /**
     * Reads the GREETING node configuration.
     *
     * @param configurationJson node configuration JSON
     * @return parsed configuration
     */
    private Map<String, Object> readConfiguration(
            String configurationJson) {

        if (configurationJson == null
                || configurationJson.isBlank()) {

            log.warn(
                    "GREETING node configuration is empty."
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        try {

            return objectMapper.readValue(
                    configurationJson,
                    Map.class
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse GREETING node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Resolves the configured greeting message.
     *
     * @param configuration parsed configuration
     * @return greeting message
     */
    private String getRequiredMessage(
            Map<String, Object> configuration) {

        Object value =
                configuration.get(
                        MESSAGE
                );

        if (value == null
                || String.valueOf(value).isBlank()) {

            log.warn(
                    "GREETING node message is missing."
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return String.valueOf(
                value
        ).trim();
    }

    /**
     * Validates the Flow execution.
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
     * Validates the Flow node.
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
     * Validates the Flow context.
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