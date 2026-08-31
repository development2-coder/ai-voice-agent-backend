package com.infinitio.aivoiceplatform.orchestrator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resolved runtime configuration for a conversation.
 *
 * <p>
 * This DTO represents the configuration that the Conversation
 * Orchestrator is allowed to use after validating the tenant,
 * agent and flow relationship.
 * </p>
 *
 * <p>
 * The response DTO is intentionally separated from request DTOs
 * because the runtime configuration is resolved by the backend
 * and must not be supplied blindly by the telephony provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRuntimeConfigurationResponseDto {

    /**
     * Tenant public identifier.
     */
    private String tenantId;

    /**
     * Agent public identifier.
     */
    private String agentId;

    /**
     * Resolved agent version.
     */
    private Integer agentVersion;

    /**
     * Flow public identifier selected for runtime.
     */
    private String flowPublicId;

    /**
     * Runtime conversation language.
     */
    private String language;

    /**
     * Configured STT provider.
     */
    private String sttProvider;

    /**
     * Configured STT model.
     */
    private String sttModel;

    /**
     * Configured LLM provider.
     */
    private String llmProvider;

    /**
     * Configured LLM model.
     */
    private String llmModel;

    /**
     * Configured TTS provider.
     */
    private String ttsProvider;

    /**
     * Configured TTS model.
     */
    private String ttsModel;

    /**
     * Configured voice/speaker.
     */
    private String voice;

    /**
     * System prompt configured for the Agent.
     */
    private String systemPrompt;
}