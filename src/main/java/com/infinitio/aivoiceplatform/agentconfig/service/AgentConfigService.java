package com.infinitio.aivoiceplatform.agentconfig.service;

import com.infinitio.aivoiceplatform.agentconfig.dto.request.CreateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.UpdateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.response.AgentConfigResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;

/**
 * Service interface for Agent Configuration.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface AgentConfigService {

    AgentConfigResponse create(
            CreateAgentConfigRequest request
    );

    AgentConfigResponse update(
            UpdateAgentConfigRequest request
    );

    AgentConfigResponse getByPublicId(
            String publicId
    );

    AgentConfigResponse getByAgent(
            String agentPublicId
    );

    PageResponse<AgentConfigResponse> getAll(
            int page,
            int size
    );

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}