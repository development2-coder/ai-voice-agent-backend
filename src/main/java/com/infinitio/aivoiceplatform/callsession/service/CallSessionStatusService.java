package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionStatusRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Handles call session lifecycle status operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionStatusService {

    /**
     * Updates the lifecycle status of a call session.
     *
     * @param callId call identifier
     * @param request status request
     * @return updated call session
     */
    CallSessionResponseDto updateStatus(
            String callId,
            UpdateCallSessionStatusRequestDto request);
}