package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;

import java.util.List;

/**
 * Service responsible for Flow node management.
 *
 * <p>
 * Handles the lifecycle of nodes that belong to a Flow.
 * Connection management is handled separately by
 * {@link FlowEdgeService}.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowNodeService {

    /**
     * Gets all active nodes belonging to a Flow.
     *
     * @param flowPublicId Flow public identifier
     * @return list of Flow nodes
     */
    List<FlowNodeResponse> getNodes(
            String flowPublicId
    );

    /**
     * Adds a node to a Flow.
     *
     * @param request node creation request
     * @return created node
     */
    FlowNodeResponse addNode(
            AddFlowNodeRequest request
    );

    /**
     * Updates an existing Flow node.
     *
     * @param request node update request
     * @return updated node
     */
    FlowNodeResponse updateNode(
            UpdateFlowNodeRequest request
    );

    /**
     * Soft-deletes a Flow node.
     *
     * @param nodePublicId node public identifier
     */
    void deleteNode(
            String nodePublicId
    );
}