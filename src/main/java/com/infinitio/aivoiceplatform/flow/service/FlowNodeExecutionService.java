package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;

import java.util.Map;

public interface FlowNodeExecutionService {

    FlowNodeExecutionResult execute(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context
    );
}