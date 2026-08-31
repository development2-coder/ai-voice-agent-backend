package com.infinitio.aivoiceplatform.orchestrator.service;

import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationRuntimeConfigurationResponseDto;

/**
 * Resolves and validates the runtime configuration required
 * to execute a conversation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ConversationRuntimeConfigurationService {

    /**
     * Resolves and validates the runtime configuration.
     *
     * @param tenantId tenant public identifier
     * @param agentId agent public identifier
     * @param agentVersion requested agent version
     * @param flowPublicId optional flow public identifier
     * @return resolved runtime configuration
     */
    ConversationRuntimeConfigurationResponseDto
    resolveRuntimeConfiguration(
            String tenantId,
            String agentId,
            Integer agentVersion,
            String flowPublicId
    );
}