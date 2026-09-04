package com.infinitio.aivoiceplatform.orchestrator.service;

import com.infinitio.aivoiceplatform.orchestrator.dto.request.EndConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationRuntimeConfigurationResponseDto;

/**
 * Handles Call Session lifecycle operations for the
 * Conversation Orchestrator.
 *
 * <p>
 * This service coordinates existing Call Session services.
 * It does not directly access the Call Session repository.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ConversationSessionService {

    /**
     * Starts a conversation by creating or reusing a Call Session
     * and starting the configured Flow.
     *
     * @param request conversation start request
     * @param runtimeConfiguration resolved trusted Agent runtime configuration
     * @return conversation runtime response
     */
    ConversationOrchestratorResponseDto startConversation(
            StartConversationRequestDto request,
            ConversationRuntimeConfigurationResponseDto
                    runtimeConfiguration
    );

    /**
     * Ends an active conversation.
     *
     * @param request conversation end request
     * @return conversation runtime response
     */
    ConversationOrchestratorResponseDto endConversation(
            EndConversationRequestDto request
    );
}