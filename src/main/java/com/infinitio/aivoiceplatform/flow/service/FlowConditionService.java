package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;

import java.util.List;
import java.util.Map;

public interface FlowConditionService {

    FlowEdge findMatchingEdge(
            List<FlowEdge> edges,
            Map<String, Object> context
    );

    boolean evaluate(
            String expression,
            Map<String, Object> context
    );
}