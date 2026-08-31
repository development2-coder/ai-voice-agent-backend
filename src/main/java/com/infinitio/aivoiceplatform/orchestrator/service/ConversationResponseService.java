package com.infinitio.aivoiceplatform.orchestrator.service;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;

/**
 * Builds responses returned by the Conversation Orchestrator.
 *
 * <p>
 * This service converts internal Flow execution results into
 * the public Conversation Orchestrator response model.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ConversationResponseService {

    /**
     * Builds a conversation response from a Flow execution.
     *
     * @param callId call identifier
     * @param transcript caller transcript
     * @param execution Flow execution result
     * @return conversation response
     */
    ConversationOrchestratorResponseDto buildResponse(
            String callId,
            String transcript,
            FlowExecutionResult execution
    );

    /**
     * Builds a completed conversation response.
     *
     * @param callId call identifier
     * @return completed conversation response
     */
    ConversationOrchestratorResponseDto buildCompletedResponse(
            String callId
    );
}