package com.infinitio.aivoiceplatform.agent.service;

import com.infinitio.aivoiceplatform.agent.dto.request.CopyAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentResponse;

/**
 * Service responsible for creating complete Agent copies.
 *
 * <p>
 * The copy operation includes Agent Configuration, Prompts and
 * Flow Builder definitions including nodes and edges.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface AgentCopyService {

    /**
     * Creates a complete copy of an existing Agent.
     *
     * @param publicId source Agent public identifier
     * @param request copy request
     * @return copied Agent
     */
    AgentResponse copy(
            String publicId,
            CopyAgentRequest request
    );
}