package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowEdgeResponse;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowEdgeRequest;
import java.util.List;

/**
 * Service responsible for Flow edge management.
 *
 * <p>
 * A Flow edge represents a directed connection between an
 * output port of one node and an input port of another node.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowEdgeService {

    /**
     * Gets all active edges belonging to a Flow.
     *
     * @param flowPublicId Flow public identifier
     * @return active Flow edges
     */
    List<FlowEdgeResponse> getEdges(
            String flowPublicId
    );

    /**
     * Creates a port-aware Flow edge.
     *
     * @param request edge creation request
     * @return created edge
     */
    FlowEdgeResponse addEdge(
            AddFlowEdgeRequest request
    );

    /**
     * Soft-deletes an edge.
     *
     * @param edgePublicId edge public identifier
     */
    void deleteEdge(
            String edgePublicId
    );

    /**
     * Updates an existing Flow edge.
     *
     * @param request edge update request
     * @return updated edge
     */
    FlowEdgeResponse updateEdge(
            UpdateFlowEdgeRequest request
    );
}