package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmMessageDto;
import com.infinitio.aivoiceplatform.llm.service.LlmRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flow node handler for LLM generation.
 *
 * <p>
 * This handler delegates LLM execution to the existing
 * {@link LlmRuntimeService}. It does not communicate directly
 * with OpenAI or any other LLM provider.
 * </p>
 *
 * <p>
 * The node supports multiple Flow execution patterns. An LLM node
 * can receive an existing conversation, use a prompt configured
 * directly on the node, or use the latest transcript from the
 * Flow context.
 * </p>
 *
 * <p>
 * Message resolution order:
 * </p>
 *
 * <ol>
 *     <li>Existing conversation messages</li>
 *     <li>Prompt configured on the LLM node</li>
 *     <li>Latest transcript from Flow context</li>
 * </ol>
 *
 * <p>
 * This allows both user-first and AI-first client-defined flows.
 * </p>
 *
 * <p>
 * Supported Flow context values:
 * </p>
 *
 * <pre>
 * callId
 * language
 * transcript
 * conversationMessages
 * </pre>
 *
 * <p>
 * Supported node configuration values:
 * </p>
 *
 * <pre>
 * prompt
 * finalResponse
 * </pre>
 *
 * <p>
 * Result values stored in Flow context:
 * </p>
 *
 * <pre>
 * llmResponse
 * lastLlmResponse
 * conversationMessages
 * language
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmNodeHandler
        implements FlowNodeHandler {

    /**
     * Call identifier context key.
     */
    private static final String CALL_ID =
            "callId";

    /**
     * Language context key.
     */
    private static final String LANGUAGE =
            "language";

    /**
     * Transcript context key.
     */
    private static final String TRANSCRIPT =
            "transcript";

    /**
     * Conversation messages context key.
     */
    private static final String CONVERSATION_MESSAGES =
            "conversationMessages";

    /**
     * Final response configuration key.
     */
    private static final String FINAL_RESPONSE =
            "finalResponse";

    /**
     * LLM prompt configuration key.
     */
    private static final String PROMPT =
            "prompt";

    /**
     * LLM response context key.
     */
    private static final String LLM_RESPONSE =
            "llmResponse";

    /**
     * Last LLM response context key.
     */
    private static final String LAST_LLM_RESPONSE =
            "lastLlmResponse";

    /**
     * Node execution action.
     */
    private static final String ACTION =
            "LLM";

    /**
     * Assistant message role.
     */
    private static final String ASSISTANT_ROLE =
            "assistant";

    /**
     * User message role.
     */
    private static final String USER_ROLE =
            "user";

    /**
     * JSON message role property.
     */
    private static final String ROLE =
            "role";

    /**
     * JSON message content property.
     */
    private static final String CONTENT =
            "content";

    /**
     * JSON configuration class.
     */
    private final ObjectMapper objectMapper;

    /**
     * Flow context service used for variable resolution.
     */
    private final FlowContextService flowContextService;

    /**
     * Existing provider-independent LLM runtime service.
     */
    private final LlmRuntimeService llmRuntimeService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.LLM;
    }

    /**
     * Executes the LLM Flow node.
     *
     * <p>
     * The node does not assume that the conversation must start
     * with user input. It can execute with a configured prompt,
     * an existing conversation, or a transcript.
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
                "Executing LLM Flow node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String callId =
                getRequiredContextString(
                        context,
                        CALL_ID
                );

        String language =
                getOptionalContextString(
                        context,
                        LANGUAGE
                );

        boolean finalResponse =
                getBooleanConfigurationValue(
                        configuration,
                        FINAL_RESPONSE,
                        true
                );

        List<LlmMessageDto> messages =
                resolveMessages(
                        context,
                        configuration
                );

        validateMessages(
                messages
        );

        LlmGenerationRequestDto request =
                LlmGenerationRequestDto.builder()
                        .callId(
                                callId
                        )
                        .language(
                                language
                        )
                        .messages(
                                messages
                        )
                        .finalResponse(
                                finalResponse
                        )
                        .build();

        log.debug(
                "Calling LLM runtime service. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "callId={}, language={}, messages={}, " +
                        "finalResponse={}",
                execution.getPublicId(),
                node.getNodeKey(),
                callId,
                language,
                messages.size(),
                finalResponse
        );

        LlmGenerationResponseDto response =
                llmRuntimeService.generate(
                        request
                );

        validateResponse(
                response
        );

        String content =
                response.getContent();

        if (content == null
                || content.isBlank()) {

            log.warn(
                    "LLM runtime returned an empty response. " +
                            "executionPublicId={}, nodeKey={}",
                    execution.getPublicId(),
                    node.getNodeKey()
            );
        }

        context.put(
                LLM_RESPONSE,
                content
        );

        context.put(
                LAST_LLM_RESPONSE,
                response
        );

        /*
         * Preserve the generated assistant response in the
         * conversation context for subsequent LLM nodes
         * and subsequent conversation turns.
         */
        if (content != null
                && !content.isBlank()) {

            messages.add(
                    LlmMessageDto.builder()
                            .role(
                                    ASSISTANT_ROLE
                            )
                            .content(
                                    content
                            )
                            .build()
            );
        }

        context.put(
                CONVERSATION_MESSAGES,
                messages
        );

        /*
         * Preserve the effective language returned by the
         * LLM runtime for downstream nodes such as TTS.
         */
        if (response.getLanguage() != null
                && !response.getLanguage().isBlank()) {

            context.put(
                    LANGUAGE,
                    response.getLanguage()
            );
        }

        log.info(
                "LLM Flow node completed. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "provider={}, model={}, latencyMs={}, " +
                        "inputTokens={}, outputTokens={}, " +
                        "responseLength={}",
                execution.getPublicId(),
                node.getNodeKey(),
                response.getProvider(),
                response.getModel(),
                response.getLatencyMs(),
                response.getInputTokens(),
                response.getOutputTokens(),
                content == null
                        ? 0
                        : content.length()
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        ACTION
                )
                .outputText(
                        content
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(
                        context
                )
                .build();
    }

    // =========================================================
    // MESSAGE RESOLUTION
    // =========================================================

    /**
     * Resolves messages for the LLM request.
     *
     * <p>
     * The Flow can reach an LLM node from different sources.
     * No fixed conversation order is assumed.
     * </p>
     *
     * <p>
     * Resolution order:
     * </p>
     *
     * <ol>
     *     <li>Existing conversation messages</li>
     *     <li>Prompt configured on the LLM node</li>
     *     <li>Latest transcript from Flow context</li>
     * </ol>
     *
     * @param context Flow execution context
     * @param configuration LLM node configuration
     * @return messages for LLM generation
     */
    private List<LlmMessageDto> resolveMessages(
            Map<String, Object> context,
            Map<String, Object> configuration) {

        Object configuredMessages =
                context.get(
                        CONVERSATION_MESSAGES
                );

        if (configuredMessages
                instanceof List<?> list) {

            List<LlmMessageDto> messages =
                    mapConversationMessages(
                            list
                    );

            if (!messages.isEmpty()) {

                log.debug(
                        "Using existing conversation messages " +
                                "for LLM node. messageCount={}",
                        messages.size()
                );

                return messages;
            }
        }

        String configuredPrompt =
                getConfigurationString(
                        configuration,
                        PROMPT
                );

        if (configuredPrompt != null
                && !configuredPrompt.isBlank()) {

            String resolvedPrompt =
                    flowContextService.replaceVariables(
                            configuredPrompt,
                            context
                    );

            if (resolvedPrompt != null
                    && !resolvedPrompt.isBlank()) {

                log.debug(
                        "Using configured LLM prompt for Flow node."
                );

                List<LlmMessageDto> messages =
                        new ArrayList<>();

                messages.add(
                        LlmMessageDto.builder()
                                .role(
                                        USER_ROLE
                                )
                                .content(
                                        resolvedPrompt
                                )
                                .build()
                );

                return messages;
            }
        }

        String transcript =
                getOptionalContextString(
                        context,
                        TRANSCRIPT
                );

        if (transcript != null
                && !transcript.isBlank()) {

            log.debug(
                    "Using Flow transcript as LLM user message."
            );

            List<LlmMessageDto> messages =
                    new ArrayList<>();

            messages.add(
                    LlmMessageDto.builder()
                            .role(
                                    USER_ROLE
                            )
                            .content(
                                    transcript
                            )
                            .build()
            );

            return messages;
        }

        log.warn(
                "No LLM input was found in Flow context or node " +
                        "configuration."
        );

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    /**
     * Converts generic conversation messages into LLM message DTOs.
     *
     * @param list conversation message list
     * @return mapped messages
     */
    private List<LlmMessageDto> mapConversationMessages(
            List<?> list) {

        List<LlmMessageDto> messages =
                new ArrayList<>();

        for (Object item : list) {

            if (item instanceof LlmMessageDto message) {

                messages.add(
                        message
                );

                continue;
            }

            if (item instanceof Map<?, ?> map) {

                messages.add(
                        mapToMessage(
                                map
                        )
                );
            }
        }

        return messages;
    }

    /**
     * Converts a generic map into an LLM message.
     *
     * @param map message map
     * @return LLM message
     */
    private LlmMessageDto mapToMessage(
            Map<?, ?> map) {

        Object role =
                map.get(
                        ROLE
                );

        Object content =
                map.get(
                        CONTENT
                );

        if (role == null
                || content == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        String resolvedRole =
                String.valueOf(
                        role
                ).trim();

        String resolvedContent =
                String.valueOf(
                        content
                ).trim();

        if (resolvedRole.isBlank()
                || resolvedContent.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return LlmMessageDto.builder()
                .role(
                        resolvedRole
                )
                .content(
                        resolvedContent
                )
                .build();
    }

    /**
     * Validates the resolved LLM messages.
     *
     * @param messages messages to validate
     */
    private void validateMessages(
            List<LlmMessageDto> messages) {

        if (messages == null
                || messages.isEmpty()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        for (LlmMessageDto message : messages) {

            if (message == null
                    || message.getRole() == null
                    || message.getRole().isBlank()
                    || message.getContent() == null
                    || message.getContent().isBlank()) {

                throw new IllegalArgumentException(
                        FlowMessages.INVALID_CONFIGURATION
                );
            }
        }
    }

    // =========================================================
    // CONFIGURATION
    // =========================================================

    /**
     * Parses the LLM node configuration.
     *
     * @param configuration JSON node configuration
     * @return configuration map
     */
    @SuppressWarnings("unchecked")
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
                    "Unable to parse LLM node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Gets a configuration string.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return configured value or null
     */
    private String getConfigurationString(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {
            return null;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isBlank()
                ? null
                : result;
    }

    /**
     * Gets a Boolean configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @param defaultValue default value
     * @return Boolean value
     */
    private boolean getBooleanConfigurationValue(
            Map<String, Object> configuration,
            String key,
            boolean defaultValue) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean booleanValue) {

            return booleanValue;
        }

        String stringValue =
                String.valueOf(
                        value
                ).trim();

        if (stringValue.isBlank()) {

            return defaultValue;
        }

        return Boolean.parseBoolean(
                stringValue
        );
    }

    // =========================================================
    // CONTEXT HELPERS
    // =========================================================

    /**
     * Gets a required string from Flow context.
     *
     * @param context Flow context
     * @param key context key
     * @return context value
     */
    private String getRequiredContextString(
            Map<String, Object> context,
            String key) {

        String result =
                getOptionalContextString(
                        context,
                        key
                );

        if (result == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return result;
    }

    /**
     * Gets an optional string from Flow context.
     *
     * @param context Flow context
     * @param key context key
     * @return context value or null
     */
    private String getOptionalContextString(
            Map<String, Object> context,
            String key) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return null;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isBlank()
                ? null
                : result;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Validates Flow execution.
     *
     * @param execution Flow execution
     */
    private void validateExecution(
            FlowExecution execution) {

        if (execution != null) {
            return;
        }

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    /**
     * Validates Flow node.
     *
     * @param node Flow node
     */
    private void validateNode(
            FlowNode node) {

        if (node != null) {
            return;
        }

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    /**
     * Validates Flow execution context.
     *
     * @param context Flow context
     */
    private void validateContext(
            Map<String, Object> context) {

        if (context != null) {
            return;
        }

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    /**
     * Validates LLM runtime response.
     *
     * @param response LLM response
     */
    private void validateResponse(
            LlmGenerationResponseDto response) {

        if (response != null) {
            return;
        }

        log.error(
                "LLM runtime returned null response."
        );

        throw new IllegalStateException(
                FlowMessages.EXECUTION_FAILED
        );
    }
}