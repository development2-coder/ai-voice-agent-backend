package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.request.*;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowTypeResponse;

import java.util.List;

/**
 * Main Flow management service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowService {

    FlowResponse create(
            CreateFlowRequest request
    );

    FlowResponse update(
            UpdateFlowRequest request
    );

    FlowResponse getByPublicId(
            String publicId
    );

    List<FlowNodeResponse> getNodes(
            String flowPublicId
    );

    FlowNodeResponse addNode(
            AddFlowNodeRequest request
    );

    FlowNodeResponse updateNode(
            UpdateFlowNodeRequest request
    );

    void deleteNode(
            String nodePublicId
    );

    void activate(
            String publicId
    );

    void deactivate(
            String publicId
    );

    void delete(
            String publicId
    );

    List<FlowTypeResponse> getFlowTypes();

    /**
     * Retrieves the complete Flow definition including
     * Flow metadata, nodes and edges.
     *
     * @param publicId Flow public identifier
     * @return complete Flow definition
     */
    FlowDefinitionResponse getDefinition(
            String publicId
    );

    /**
     * Retrieves the latest active Flow belonging to an Agent.
     *
     * @param agentPublicId Agent public identifier
     * @return Flow response
     */
    FlowResponse getLatestByAgentPublicId(
            String agentPublicId
    );

    /**
     * Saves the complete Flow Builder workspace.
     *
     * <p>
     * The submitted workspace represents the complete current
     * state of the visual Flow Builder. Existing nodes and edges
     * are updated or removed as required, while new nodes and
     * edges are created.
     * </p>
     *
     * @param publicId Flow public identifier
     * @param request complete workspace request
     * @return updated Flow definition
     */
    FlowDefinitionResponse saveWorkspace(
            String publicId,
            SaveFlowWorkspaceRequest request
    );

}