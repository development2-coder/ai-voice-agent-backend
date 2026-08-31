package com.infinitio.aivoiceplatform.orchestrator.service;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;

/**
 * Handles AI processing for Conversation Orchestrator.
 *
 * <p>
 * This service coordinates AI waiting states by resolving the
 * prompt from Flow context, invoking the configured LLM runtime,
 * storing the AI response and continuing Flow execution.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ConversationAiService {

    /**
     * Processes a Flow execution waiting for an AI response.
     *
     * @param callId call identifier
     * @param execution current Flow execution
     * @return continued Flow execution
     */
    FlowExecutionResult processAiWaitingState(
            String callId,
            FlowExecutionResult execution
    );
}