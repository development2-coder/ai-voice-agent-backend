package com.infinitio.aivoiceplatform.orchestrator.service.impl;

import com.infinitio.aivoiceplatform.callsession.dto.request.AddConversationMessageRequestDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionConversationService;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueAiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionService;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmMessageDto;
import com.infinitio.aivoiceplatform.llm.service.LlmRuntimeService;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorConstants;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorMessages;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of Conversation AI Service.
 *
 * <p>
 * Coordinates AI processing between the Flow Engine and the
 * configured LLM runtime.
 * </p>
 *
 * <p>
 * The Flow Engine decides when an AI node requires execution.
 * This service only executes the AI runtime and returns the
 * generated response back to the Flow Engine.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationAiServiceImpl
        implements ConversationAiService {

    private static final String LANGUAGE =
            "language";

    private static final String CONVERSATION_MESSAGES =
            "conversationMessages";

    private static final String AI_PROMPT =
            FlowExecutionContextKeys.AI_PROMPT;

    private static final String AI_RESPONSE =
            "aiResponse";

    private static final String USER_ROLE =
            ConversationOrchestratorConstants.ROLE_USER;

    private static final String ASSISTANT_ROLE =
            ConversationOrchestratorConstants.ROLE_ASSISTANT;

    private final LlmRuntimeService
            llmRuntimeService;

    private final FlowExecutionService
            flowExecutionService;

    private final CallSessionConversationService
            callSessionConversationService;

    private static final String SYSTEM_ROLE =
            ConversationOrchestratorConstants.ROLE_SYSTEM;

    private static final String LAST_USER_INPUT =
            "lastUserInput";

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult processAiWaitingState(
            String callId,
            FlowExecutionResult execution) {

        validateExecution(
                callId,
                execution
        );

        Map<String, Object> context =
                execution.getContext();

        if (context == null) {

            log.error(
                    "AI execution context is missing. " +
                            "callId={}, executionPublicId={}",
                    callId,
                    execution.getExecutionPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_PROCESSING_FAILED
            );
        }

        String prompt =
                resolvePrompt(
                        callId,
                        execution,
                        context
                );

        String language =
                resolveLanguage(
                        context
                );

        List<LlmMessageDto> messages =
                resolveMessages(
                        context,
                        prompt
                );

        log.info(
                "Starting LLM execution for AI node. " +
                        "callId={}, executionPublicId={}, " +
                        "node={}, language={}, messageCount={}",
                callId,
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey(),
                language,
                messages.size()
        );

        log.info(
                "Starting LLM execution. callId={}, executionPublicId={}, " +
                        "promptPresent={}, messageCount={}, lastUserInputPresent={}",
                callId,
                execution.getExecutionPublicId(),
                prompt != null && !prompt.isBlank(),
                messages.size(),
                context.get(LAST_USER_INPUT) != null
                        && !String.valueOf(
                        context.get(LAST_USER_INPUT)
                ).isBlank()
        );

        long startTime =
                System.currentTimeMillis();

        LlmGenerationResponseDto llmResponse =
                llmRuntimeService.generate(
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
                                        true
                                )
                                .build()
                );

        long latencyMs =
                System.currentTimeMillis()
                        - startTime;

        validateLlmResponse(
                callId,
                execution,
                llmResponse
        );

        log.info(
                "LLM execution completed. " +
                        "callId={}, executionPublicId={}, " +
                        "provider={}, model={}, latencyMs={}, " +
                        "responseLength={}",
                callId,
                execution.getExecutionPublicId(),
                llmResponse.getProvider(),
                llmResponse.getModel(),
                latencyMs,
                llmResponse.getContent().length()
        );

        storeAiMessage(
                callId,
                llmResponse.getContent()
        );

        FlowExecutionResult continuedExecution =
                continueFlow(
                        callId,
                        execution,
                        llmResponse
                );

        log.info(
                "Flow continued successfully after AI response. " +
                        "callId={}, executionPublicId={}, " +
                        "currentNode={}, status={}, " +
                        "waitingForInput={}, waitingForAi={}, completed={}",
                callId,
                continuedExecution.getExecutionPublicId(),
                continuedExecution.getCurrentNodeKey(),
                continuedExecution.getStatus(),
                continuedExecution.isWaitingForInput(),
                continuedExecution.isWaitingForAi(),
                continuedExecution.isCompleted()
        );

        return continuedExecution;
    }

    /**
     * Resolves the AI prompt from Flow context.
     */
    private String resolvePrompt(
            String callId,
            FlowExecutionResult execution,
            Map<String, Object> context) {

        Object promptValue =
                context.get(
                        AI_PROMPT
                );

        if (promptValue == null
                || String.valueOf(
                promptValue
        ).isBlank()) {

            log.error(
                    "AI prompt is missing from Flow context. " +
                            "callId={}, executionPublicId={}",
                    callId,
                    execution.getExecutionPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .LLM_RESPONSE_EMPTY
            );
        }

        return String.valueOf(
                promptValue
        ).trim();
    }

    /**
     * Resolves the language used by the LLM runtime.
     */
    private String resolveLanguage(
            Map<String, Object> context) {

        Object languageValue =
                context.get(
                        LANGUAGE
                );

        if (languageValue == null
                || String.valueOf(
                languageValue
        ).isBlank()) {

            return ConversationOrchestratorConstants
                    .DEFAULT_LANGUAGE;
        }

        return String.valueOf(
                languageValue
        ).trim();
    }

    /**
     * Resolves conversation messages from Flow context.
     *
     * <p>
     * Existing conversation messages are preserved and the
     * current AI prompt is added as the latest user message.
     * </p>
     */
    /**
     * Resolves messages for the AI response request.
     *
     * <p>
     * The configured AI prompt is sent as a system instruction,
     * while the latest caller input is sent as the user message.
     * Existing conversation messages are preserved when available.
     * </p>
     *
     * @param context Flow execution context
     * @param prompt AI system instruction
     * @return messages for LLM generation
     */
    /**
     * Resolves messages for the AI response request.
     *
     * <p>
     * The configured AI prompt is sent as a system instruction.
     * If customer input is already available, it is added as the
     * user message. For outbound AI-first flows, where no customer
     * input exists yet, a generic opening instruction is used so
     * that the AI can generate the first response without waiting
     * for customer speech.
     * </p>
     *
     * @param context Flow execution context
     * @param prompt AI system instruction
     * @return messages for LLM generation
     */
    private List<LlmMessageDto> resolveMessages(
            Map<String, Object> context,
            String prompt) {

        List<LlmMessageDto> messages =
                new ArrayList<>();

        /*
         * The configured AI prompt contains the business
         * instructions for the LLM.
         *
         * It must be sent as a SYSTEM message and must not
         * be treated as customer input.
         */
        if (prompt != null
                && !prompt.isBlank()) {

            messages.add(
                    LlmMessageDto.builder()
                            .role(
                                    SYSTEM_ROLE
                            )
                            .content(
                                    prompt
                            )
                            .build()
            );
        }

        /*
         * Preserve existing conversation messages when
         * available.
         */
        Object configuredMessages =
                context.get(
                        CONVERSATION_MESSAGES
                );

        if (configuredMessages instanceof List<?> list) {

            for (Object item : list) {

                if (item instanceof LlmMessageDto message) {

                    messages.add(
                            message
                    );

                    continue;
                }

                if (item instanceof Map<?, ?> map) {

                    Object role =
                            map.get("role");

                    Object content =
                            map.get("content");

                    if (role != null
                            && content != null) {

                        String roleValue =
                                String.valueOf(
                                        role
                                ).trim();

                        String contentValue =
                                String.valueOf(
                                        content
                                ).trim();

                        if (!roleValue.isBlank()
                                && !contentValue.isBlank()) {

                            messages.add(
                                    LlmMessageDto.builder()
                                            .role(
                                                    roleValue
                                            )
                                            .content(
                                                    contentValue
                                            )
                                            .build()
                            );
                        }
                    }
                }
            }
        }

        /*
         * Check whether the customer has already spoken.
         *
         * For an outbound AI-first Flow this value is normally
         * absent because the AI_RESPONSE node is the first
         * conversational node.
         */
        Object userInputValue =
                context.get(
                        LAST_USER_INPUT
                );

        String userInput =
                userInputValue == null
                        ? null
                        : String.valueOf(
                        userInputValue
                ).trim();

        /*
         * CUSTOMER-FIRST / SUBSEQUENT TURN
         *
         * If customer input exists, use it as the actual
         * user message.
         */
        if (userInput != null
                && !userInput.isBlank()) {

            messages.add(
                    LlmMessageDto.builder()
                            .role(
                                    USER_ROLE
                            )
                            .content(
                                    userInput
                            )
                            .build()
            );

            log.debug(
                    "Resolved AI messages with customer input. " +
                            "systemPromptPresent={}, " +
                            "userInputLength={}, messageCount={}",
                    prompt != null
                            && !prompt.isBlank(),
                    userInput.length(),
                    messages.size()
            );

            return messages;
        }

        /*
         * AI-FIRST OUTBOUND FLOW
         *
         * No customer input exists yet. This is expected for
         * outbound calls where the AI must speak first.
         *
         * Add a generic user instruction so that the LLM has
         * an explicit conversational generation request while
         * keeping the configured Flow prompt as the system
         * instruction.
         */
        messages.add(
                LlmMessageDto.builder()
                        .role(
                                USER_ROLE
                        )
                        .content(
                                "Start the conversation with the customer."
                        )
                        .build()
        );

        log.debug(
                "Resolved AI-first outbound messages. " +
                        "systemPromptPresent={}, messageCount={}",
                prompt != null
                        && !prompt.isBlank(),
                messages.size()
        );

        return messages;
    }

    /**
     * Stores generated AI response in conversation history.
     */
    private void storeAiMessage(
            String callId,
            String response) {

        try {

            callSessionConversationService
                    .addConversationMessage(
                            callId,
                            AddConversationMessageRequestDto.builder()
                                    .role(
                                            ASSISTANT_ROLE
                                    )
                                    .text(
                                            response
                                    )
                                    .build()
                    );

            log.debug(
                    "AI response stored in conversation history. " +
                            "callId={}",
                    callId
            );

        } catch (Exception exception) {

            /*
             * Conversation-history persistence failure should not
             * prevent the Flow from continuing after a successful
             * LLM response.
             */
            log.warn(
                    "Unable to store AI response in conversation history. " +
                            "callId={}",
                    callId,
                    exception
            );
        }
    }

    /**
     * Continues the Flow with the generated AI response.
     */
    private FlowExecutionResult continueFlow(
            String callId,
            FlowExecutionResult execution,
            LlmGenerationResponseDto llmResponse) {

        ContinueAiResponseRequest request =
                new ContinueAiResponseRequest();

        request.setExecutionPublicId(
                execution.getExecutionPublicId()
        );

        request.setResponse(
                llmResponse.getContent()
        );

        Map<String, Object> aiContext =
                new java.util.HashMap<>();

        aiContext.put(
                AI_RESPONSE,
                llmResponse.getContent()
        );

        aiContext.put(
                FlowExecutionContextKeys.LLM_RESPONSE,
                llmResponse.getContent()
        );

        aiContext.put(
                "lastAiResponse",
                llmResponse.getContent()
        );

        request.setContext(
                aiContext
        );

        FlowExecutionResult continuedExecution =
                flowExecutionService
                        .continueWithAiResponse(
                                request
                        );

        if (continuedExecution == null) {

            log.error(
                    "Flow returned null after AI continuation. " +
                            "callId={}, executionPublicId={}",
                    callId,
                    execution.getExecutionPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        return continuedExecution;
    }

    /**
     * Validates the Flow Execution.
     */
    private void validateExecution(
            String callId,
            FlowExecutionResult execution) {

        if (execution == null) {

            log.error(
                    "AI processing requested with null Flow Execution. " +
                            "callId={}",
                    callId
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        if (!execution.isWaitingForAi()) {

            log.warn(
                    "AI processing requested while Flow Execution " +
                            "is not waiting for AI. callId={}, " +
                            "executionPublicId={}, status={}",
                    callId,
                    execution.getExecutionPublicId(),
                    execution.getStatus()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_PROCESSING_FAILED
            );
        }
    }

    /**
     * Validates the LLM response.
     */
    private void validateLlmResponse(
            String callId,
            FlowExecutionResult execution,
            LlmGenerationResponseDto llmResponse) {

        if (llmResponse == null
                || llmResponse.getContent() == null
                || llmResponse.getContent().isBlank()) {

            log.error(
                    "LLM returned empty response. " +
                            "callId={}, executionPublicId={}",
                    callId,
                    execution.getExecutionPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .LLM_RESPONSE_EMPTY
            );
        }
    }
}