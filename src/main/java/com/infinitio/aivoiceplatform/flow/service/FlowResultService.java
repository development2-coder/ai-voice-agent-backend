package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;

public interface FlowResultService {

    FlowExecutionResult buildResult(
            FlowExecution execution,
            FlowNode node,
            FlowNodeExecutionResult nodeResult
    );
}