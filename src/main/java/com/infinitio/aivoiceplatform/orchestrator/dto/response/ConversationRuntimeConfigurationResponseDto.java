package com.infinitio.aivoiceplatform.orchestrator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    private String tenantId;

    private String agentId;

    private Integer agentVersion;

    private String flowPublicId;

    private String language;

    private String sttProvider;

    private String sttModel;

    private String llmProvider;

    private String llmModel;

    private String ttsProvider;

    private String ttsModel;

    private String voice;

    private String systemPrompt;

    private BigDecimal temperature;

    private Integer maxTokens;
}