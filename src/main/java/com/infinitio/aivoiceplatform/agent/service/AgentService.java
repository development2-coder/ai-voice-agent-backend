package com.infinitio.aivoiceplatform.agent.service;

import com.infinitio.aivoiceplatform.agent.dto.request.CreateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.request.UpdateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentResponse;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentWorkspaceResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;

/**
 * Service interface for Agent.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface AgentService {

    AgentResponse create(CreateAgentRequest request);

    AgentResponse update(UpdateAgentRequest request);

    AgentResponse getByPublicId(String publicId);

    PageResponse<AgentResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);

    /**
     * Retrieves the complete Agent workspace for the visual
     * Flow Builder.
     *
     * @param publicId Agent public identifier
     * @return Agent workspace
     */
    AgentWorkspaceResponse getWorkspace(
            String publicId
    );
}