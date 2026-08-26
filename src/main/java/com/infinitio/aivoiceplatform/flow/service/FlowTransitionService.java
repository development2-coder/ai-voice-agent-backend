package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.entity.FlowNode;

import java.util.Map;

public interface FlowTransitionService {

    FlowNode getNextNode(
            FlowNode currentNode,
            Map<String, Object> context
    );
}