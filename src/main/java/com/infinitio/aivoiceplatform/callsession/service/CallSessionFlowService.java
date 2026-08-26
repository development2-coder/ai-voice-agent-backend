package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateFlowStateRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Handles flow state operations for call sessions.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionFlowService {

    /**
     * Updates the current flow node.
     *
     * @param callId call identifier
     * @param request flow state request
     * @return updated call session
     */
    CallSessionResponseDto updateFlowState(
            String callId,
            UpdateFlowStateRequestDto request);
}