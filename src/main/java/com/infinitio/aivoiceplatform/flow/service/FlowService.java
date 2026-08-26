package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.request.*;
import com.infinitio.aivoiceplatform.flow.dto.response.*;

import java.util.List;

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

    List<FlowEdgeResponse> getEdges(
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

    FlowEdgeResponse addEdge(
            AddFlowEdgeRequest request
    );

    void deleteEdge(
            String edgePublicId
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
}