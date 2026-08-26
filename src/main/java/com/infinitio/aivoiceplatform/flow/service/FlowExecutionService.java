package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.request.ContinueFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.StartFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueApiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueAiResponseRequest;
public interface FlowExecutionService {

    FlowExecutionResult start(
            StartFlowExecutionRequest request
    );

    FlowExecutionResult continueExecution(
            ContinueFlowExecutionRequest request
    );

    FlowExecutionResult getExecution(
            String executionPublicId
    );

    FlowExecutionResult continueWithApiResponse(
            ContinueApiResponseRequest request
    );

    FlowExecutionResult continueWithAiResponse(
            ContinueAiResponseRequest request
    );

    void cancel(
            String executionPublicId
    );
}