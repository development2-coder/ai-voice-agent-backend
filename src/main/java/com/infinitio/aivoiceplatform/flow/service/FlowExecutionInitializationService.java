package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.request.StartFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;

public interface FlowExecutionInitializationService {

    /**
     * Creates and commits the initial Flow Execution
     * without executing the Flow runtime.
     *
     * @param request Flow start request
     * @return initialized Flow execution
     */
    FlowExecutionResult initialize(
            StartFlowExecutionRequest request
    );
}