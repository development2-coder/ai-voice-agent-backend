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
import com.infinitio.aivoiceplatform.callsession.dto.CallConversationMessageDto;
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

        prompt =
                applyLanguageInstruction(
                        prompt,
                        language
                );

        List<LlmMessageDto> messages =
                resolveMessages(
                        callId,
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
    /**
     * Resolves the messages that should be sent to the LLM.
     *
     * <p>
     * The configured AI prompt is always sent as the SYSTEM message.
     * Existing conversation history is then loaded from the call-session
     * conversation storage so the LLM can understand previous turns.
     * </p>
     *
     * <p>
     * The latest caller transcript is already persisted by
     * ConversationInputService before this method is called. Therefore,
     * when persisted history is available, the latest user message must
     * not be added again from lastUserInput.
     * </p>
     *
     * @param callId call public identifier
     * @param context Flow execution context
     * @param prompt AI system instruction
     * @return messages for LLM generation
     */
    private List<LlmMessageDto> resolveMessages(
            String callId,
            Map<String, Object> context,
            String prompt) {

        final int MAX_HISTORY_MESSAGES = 20;

        List<LlmMessageDto> messages =
                new ArrayList<>();

        /*
         * The configured AI prompt is the SYSTEM instruction.
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
         * Primary source:
         *
         * Read the actual persisted conversation history.
         *
         * ConversationInputService stores the customer's message
         * before Flow execution reaches the AI node.
         *
         * ConversationAiServiceImpl also stores every AI response.
         */
        List<CallConversationMessageDto> storedMessages =
                callSessionConversationService
                        .getConversationMessages(
                                callId
                        );

        if (storedMessages != null
                && !storedMessages.isEmpty()) {

            int startIndex =
                    Math.max(
                            0,
                            storedMessages.size()
                                    - MAX_HISTORY_MESSAGES
                    );

            for (int index = startIndex;
                 index < storedMessages.size();
                 index++) {

                CallConversationMessageDto storedMessage =
                        storedMessages.get(
                                index
                        );

                if (storedMessage == null
                        || storedMessage.getRole() == null
                        || storedMessage.getText() == null
                        || storedMessage.getText().isBlank()) {

                    continue;
                }

                String role =
                        storedMessage
                                .getRole()
                                .trim();

                if (!USER_ROLE.equals(role)
                        && !ASSISTANT_ROLE.equals(role)) {

                    continue;
                }

                messages.add(
                        LlmMessageDto.builder()
                                .role(
                                        role
                                )
                                .content(
                                        storedMessage
                                                .getText()
                                )
                                .build()
                );
            }

            log.info(
                    "Loaded conversation history for LLM. " +
                            "callId={}, storedMessages={}, " +
                            "usedMessages={}, messageCount={}",
                    callId,
                    storedMessages.size(),
                    Math.min(
                            storedMessages.size(),
                            MAX_HISTORY_MESSAGES
                    ),
                    messages.size()
            );

            return messages;
        }

        /*
         * Fallback:
         *
         * Some executions may already contain conversationMessages
         * inside the Flow context.
         */
        Object configuredMessages =
                context.get(
                        CONVERSATION_MESSAGES
                );

        if (configuredMessages instanceof List<?> list) {

            for (Object item : list) {

                if (item instanceof LlmMessageDto message) {

                    if (message.getRole() != null
                            && message.getContent() != null
                            && !message.getContent().isBlank()) {

                        messages.add(
                                message
                        );
                    }

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
                                && !contentValue.isBlank()
                                && (USER_ROLE.equals(roleValue)
                                || ASSISTANT_ROLE.equals(roleValue))) {

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

            if (messages.size() > 1) {

                log.info(
                        "Using conversation messages from Flow context. " +
                                "callId={}, messageCount={}",
                        callId,
                        messages.size()
                );

                return messages;
            }
        }

        /*
         * Final fallback:
         *
         * Use lastUserInput when no persisted conversation history
         * or Flow-context history is available.
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
                    "Resolved AI messages using lastUserInput fallback. " +
                            "callId={}, messageCount={}",
                    callId,
                    messages.size()
            );

            return messages;
        }

        /*
         * AI-FIRST outbound flow.
         *
         * No customer input exists yet, so give the LLM an explicit
         * instruction to start the conversation.
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
                        "callId={}, systemPromptPresent={}, " +
                        "messageCount={}",
                callId,
                prompt != null
                        && !prompt.isBlank(),
                messages.size()
        );

        return messages;
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

    /**
     * Stores the AI-generated response in the call conversation history.
     *
     * @param callId public call identifier
     * @param response AI-generated response text
     */
    private void storeAiMessage(
            String callId,
            String response) {

        if (callId == null || callId.isBlank()) {
            log.warn(
                    "Cannot store AI message because callId is missing."
            );
            return;
        }

        if (response == null || response.isBlank()) {
            log.warn(
                    "Cannot store empty AI response. callId={}",
                    callId
            );
            return;
        }

        callSessionConversationService
                .addConversationMessage(
                        callId,
                        AddConversationMessageRequestDto
                                .builder()
                                .role(
                                        ConversationOrchestratorConstants
                                                .ROLE_ASSISTANT
                                )
                                .text(
                                        response
                                )
                                .build()
                );

        log.debug(
                "AI response stored in conversation history. " +
                        "callId={}, responseLength={}",
                callId,
                response.length()
        );
    }

    /**
     * Adds the runtime language instruction to the AI prompt.
     *
     * @param prompt configured AI prompt
     * @param language detected conversation language
     * @return prompt with runtime language instruction
     */
    private String applyLanguageInstruction(
            String prompt,
            String language) {

        if (prompt == null
                || prompt.isBlank()) {

            return prompt;
        }

        String languageName =
                resolveLanguageName(
                        language
                );

        return prompt
                + "\n\n"
                + "IMPORTANT LANGUAGE RULE:\n"
                + "Respond to the customer only in "
                + languageName
                + ".\n"
                + "Use the customer's detected language for this turn. "
                + "Do not continue using the previous language if the "
                + "customer has switched languages.";
    }

    /**
     * Converts language code into a language name understood by the LLM.
     *
     * @param language language code
     * @return language name
     */
    private String resolveLanguageName(
            String language) {

        if (language == null
                || language.isBlank()) {

            return "the customer's language";
        }

        String normalized =
                language
                        .trim()
                        .toLowerCase(
                                java.util.Locale.ROOT
                        );

        if (normalized.startsWith("mr")) {

            return "Marathi";
        }

        if (normalized.startsWith("en")) {

            return "English";
        }

        if (normalized.startsWith("hi")) {

            return "Hindi";
        }

        if (normalized.startsWith("ta")) {

            return "Tamil";
        }

        if (normalized.startsWith("te")) {

            return "Telugu";
        }

        if (normalized.startsWith("kn")) {

            return "Kannada";
        }

        if (normalized.startsWith("gu")) {

            return "Gujarati";
        }

        if (normalized.startsWith("bn")) {

            return "Bengali";
        }

        return language;
    }
}