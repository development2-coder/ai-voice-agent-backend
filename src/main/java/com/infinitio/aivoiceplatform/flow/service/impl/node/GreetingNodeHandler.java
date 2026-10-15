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

    /**
     * Runtime configuration key containing the Agent welcome message.
     */
    private static final String WELCOME_MESSAGE =
            "welcomeMessage";

    /**
     * Runtime configuration key containing the Agent Configuration greeting.
     */
    private static final String GREETING_MESSAGE =
            "greetingMessage";

    /**
     * Runtime configuration key used by the conversation runtime.
     */
    private static final String RUNTIME_CONFIGURATION =
            "runtimeConfiguration";

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

        String welcomeMessage =
                resolveWelcomeMessage(
                        context
                );

        String finalGreeting =
                combineWelcomeAndGreeting(
                        welcomeMessage,
                        resolvedGreeting
                );

        /*
         * Store the generated speech text in Flow context.
         *
         * The downstream TTS node resolves text from this key
         * when no explicit text is configured on the TTS node.
         */
        context.put(
                TTS_TEXT,
                finalGreeting
        );

        log.debug(
                "GREETING text prepared for TTS. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "welcomeConfigured={}, flowGreetingConfigured={}, " +
                        "textLength={}",
                execution.getPublicId(),
                node.getNodeKey(),
                isConfigured(welcomeMessage),
                isConfigured(resolvedGreeting),
                finalGreeting.length()
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
                        finalGreeting
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    /**
     * Resolves the Agent-level welcome message from the runtime context.
     *
     * <p>
     * The welcome message is stored inside the runtime configuration
     * by the conversation session service. It is intentionally read
     * from the context instead of loading the Agent entity again.
     * </p>
     *
     * @param context current Flow context
     * @return configured welcome message, or {@code null}
     */
    /**
     * Resolves the Agent-level welcome message from the runtime context.
     *
     * <p>
     * Resolution order:
     * </p>
     *
     * <ol>
     *     <li>Agent welcome message</li>
     *     <li>Agent Configuration greeting message</li>
     * </ol>
     *
     * <p>
     * The Flow GREETING node message is handled separately and is
     * appended after this message.
     * </p>
     *
     * @param context current Flow runtime context
     * @return configured welcome message, or {@code null}
     */
    private String resolveWelcomeMessage(
            Map<String, Object> context) {

        Object runtimeConfigurationValue =
                context.get(
                        RUNTIME_CONFIGURATION
                );

        if (!(runtimeConfigurationValue
                instanceof Map<?, ?> runtimeConfiguration)) {

            log.debug(
                    "Runtime configuration is not available in Flow context. " +
                            "Agent welcome message will be skipped."
            );

            return null;
        }

        /*
         * First priority:
         * Agent creation/update welcome message.
         */
        Object welcomeValue =
                runtimeConfiguration.get(
                        WELCOME_MESSAGE
                );

        if (welcomeValue != null) {

            String welcomeMessage =
                    String.valueOf(
                            welcomeValue
                    ).trim();

            if (!welcomeMessage.isBlank()) {

                return welcomeMessage;
            }
        }

        /*
         * Second priority:
         * Agent Configuration greeting message.
         *
         * This is used only when the Agent-level welcome message
         * is not configured.
         */
        Object greetingValue =
                runtimeConfiguration.get(
                        GREETING_MESSAGE
                );

        if (greetingValue != null) {

            String greetingMessage =
                    String.valueOf(
                            greetingValue
                    ).trim();

            if (!greetingMessage.isBlank()) {

                return greetingMessage;
            }
        }

        return null;
    }

    /**
     * Combines the Agent welcome message with the Flow greeting.
     *
     * <p>
     * The Agent welcome message is played first, followed by the
     * Flow Builder greeting. Blank messages are ignored so that
     * either configuration can be omitted safely.
     * </p>
     *
     * @param welcomeMessage Agent-level welcome message
     * @param flowGreeting Flow GREETING node message
     * @return combined speech text
     */
    private String combineWelcomeAndGreeting(
            String welcomeMessage,
            String flowGreeting) {

        boolean hasWelcome =
                isConfigured(
                        welcomeMessage
                );

        boolean hasFlowGreeting =
                isConfigured(
                        flowGreeting
                );

        if (hasWelcome && hasFlowGreeting) {

            return welcomeMessage
                    + " "
                    + flowGreeting;
        }

        if (hasWelcome) {

            return welcomeMessage;
        }

        if (hasFlowGreeting) {

            return flowGreeting;
        }

        log.warn(
                "Neither Agent welcome message nor Flow greeting " +
                        "is configured."
        );

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }


    /**
     * Checks whether a text value is configured.
     *
     * @param value text value
     * @return true when the value is not null or blank
     */
    private boolean isConfigured(
            String value) {

        return value != null
                && !value.isBlank();
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