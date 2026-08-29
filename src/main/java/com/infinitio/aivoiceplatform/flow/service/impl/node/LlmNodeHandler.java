package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmMessageDto;
import com.infinitio.aivoiceplatform.llm.service.LlmRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Flow node handler for LLM generation.
 *
 * <p>
 * This handler delegates LLM execution to the existing
 * {@link LlmRuntimeService}. It does not communicate directly
 * with OpenAI or any other provider.
 * </p>
 *
 * <p>
 * The node reads the current conversation from the Flow context
 * and stores the generated response back into the context.
 * </p>
 *
 * <p>
 * Supported context values:
 * </p>
 *
 * <pre>
 * callId
 * language
 * transcript
 * conversationMessages
 * finalResponse
 * </pre>
 *
 * <p>
 * Result values:
 * </p>
 *
 * <pre>
 * llmResponse
 * lastLlmResponse
 * conversationMessages
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

    private static final String CALL_ID =
            "callId";

    private static final String LANGUAGE =
            "language";

    private static final String TRANSCRIPT =
            "transcript";

    private static final String CONVERSATION_MESSAGES =
            "conversationMessages";

    private static final String FINAL_RESPONSE =
            "finalResponse";

    private static final String LLM_RESPONSE =
            "llmResponse";

    private static final String LAST_LLM_RESPONSE =
            "lastLlmResponse";

    private static final String ACTION =
            "LLM";

    private static final String DEFAULT_LANGUAGE =
            "en-IN";

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

        String callId =
                getRequiredString(
                        context,
                        CALL_ID
                );

        String language =
                getStringOrDefault(
                        context,
                        LANGUAGE,
                        DEFAULT_LANGUAGE
                );

        boolean finalResponse =
                getBooleanValue(
                        context,
                        FINAL_RESPONSE,
                        true
                );

        List<LlmMessageDto> messages =
                resolveMessages(
                        context
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

        if (response == null) {

            log.error(
                    "LLM runtime returned null response. " +
                            "executionPublicId={}, nodeKey={}",
                    execution.getPublicId(),
                    node.getNodeKey()
            );

            throw new IllegalStateException(
                    FlowMessages.EXECUTION_FAILED
            );
        }

        String content =
                response.getContent();

        context.put(
                LLM_RESPONSE,
                content
        );

        context.put(
                LAST_LLM_RESPONSE,
                response
        );

        /*
         * Keep the generated assistant message in the
         * conversation context so that the next LLM node
         * or subsequent turn can reuse it.
         */
        messages.add(
                LlmMessageDto.builder()
                        .role(
                                "assistant"
                        )
                        .content(
                                content
                        )
                        .build()
        );

        context.put(
                CONVERSATION_MESSAGES,
                messages
        );

        /*
         * The provider may return the effective language.
         * Preserve it for downstream TTS and subsequent nodes.
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
     * Resolves conversation messages from the Flow context.
     *
     * <p>
     * If conversationMessages is already available, it is reused.
     * Otherwise the latest STT transcript is converted into a
     * user message.
     * </p>
     */
    private List<LlmMessageDto> resolveMessages(
            Map<String, Object> context) {

        Object configuredMessages =
                context.get(
                        CONVERSATION_MESSAGES
                );

        if (configuredMessages
                instanceof List<?> list) {

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

            if (!messages.isEmpty()) {

                return messages;
            }
        }

        String transcript =
                getRequiredString(
                        context,
                        TRANSCRIPT
                );

        List<LlmMessageDto> messages =
                new ArrayList<>();

        messages.add(
                LlmMessageDto.builder()
                        .role(
                                "user"
                        )
                        .content(
                                transcript
                        )
                        .build()
        );

        return messages;
    }

    /**
     * Converts a generic context map into the actual LLM
     * message DTO.
     */
    private LlmMessageDto mapToMessage(
            Map<?, ?> map) {

        Object role =
                map.get(
                        "role"
                );

        Object content =
                map.get(
                        "content"
                );

        if (role == null
                || content == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return LlmMessageDto.builder()
                .role(
                        String.valueOf(
                                role
                        )
                )
                .content(
                        String.valueOf(
                                content
                        )
                )
                .build();
    }

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
    // CONTEXT HELPERS
    // =========================================================

    private String getRequiredString(
            Map<String, Object> context,
            String key) {

        Object value =
                context.get(
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

    private String getStringOrDefault(
            Map<String, Object> context,
            String key,
            String defaultValue) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isBlank()
                ? defaultValue
                : result;
    }

    private boolean getBooleanValue(
            Map<String, Object> context,
            String key,
            boolean defaultValue) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.parseBoolean(
                String.valueOf(
                        value
                )
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateExecution(
            FlowExecution execution) {

        if (execution != null) {
            return;
        }

        throw new IllegalArgumentException(
                "Flow execution cannot be null."
        );
    }

    private void validateNode(
            FlowNode node) {

        if (node != null) {
            return;
        }

        throw new IllegalArgumentException(
                "Flow node cannot be null."
        );
    }

    private void validateContext(
            Map<String, Object> context) {

        if (context != null) {
            return;
        }

        throw new IllegalArgumentException(
                "Flow execution context cannot be null."
        );
    }
}