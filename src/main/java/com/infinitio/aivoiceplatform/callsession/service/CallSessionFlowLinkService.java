package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;

public interface CallSessionFlowLinkService {

    /**
     * Persists the active Flow Execution ID into the Call Session
     * in an independent transaction.
     *
     * @param callId call public ID
     * @param execution Flow execution
     */
    void linkExecution(
            String callId,
            FlowExecutionResult execution
    );
}