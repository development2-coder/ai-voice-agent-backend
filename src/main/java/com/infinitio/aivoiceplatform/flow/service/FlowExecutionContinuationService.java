package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.request.ContinueApiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueAiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;

/**
 * Handles continuation of an existing Flow execution.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowExecutionContinuationService {

    /**
     * Continues an execution normally.
     *
     * @param request continuation request
     * @return execution result
     */
    FlowExecutionResult continueExecution(
            ContinueFlowExecutionRequest request
    );

    /**
     * Continues an execution after an API response.
     *
     * @param request API response
     * @return execution result
     */
    FlowExecutionResult continueWithApiResponse(
            ContinueApiResponseRequest request
    );

    /**
     * Continues an execution after an AI response.
     *
     * @param request AI response
     * @return execution result
     */
    FlowExecutionResult continueWithAiResponse(
            ContinueAiResponseRequest request
    );

    /**
     * Resumes an execution after a WAIT node timer expires.
     *
     * @param executionPublicId execution public ID
     * @return execution result
     */
    FlowExecutionResult continueAfterWait(
            String executionPublicId
    );

}