package com.infinitio.aivoiceplatform.agentconfig.service;

import com.infinitio.aivoiceplatform.agentconfig.dto.response.AgentConfigurationOptionsResponse;

/**
 * Service for resolving Agent Configuration selection options.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface AgentConfigurationOptionsService {

    /**
     * Fetches all selectable Agent Configuration options.
     *
     * @return Agent Configuration options
     */
    AgentConfigurationOptionsResponse getOptions();
}