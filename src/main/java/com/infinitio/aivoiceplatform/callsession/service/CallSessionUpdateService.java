package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Handles general call session update operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionUpdateService {

    /**
     * Updates general call session information.
     *
     * @param callId call identifier
     * @param request update request
     * @return updated call session
     */
    CallSessionResponseDto updateCallSession(
            String callId,
            UpdateCallSessionRequestDto request
    );

    /**
     * Ends an active CallSession.
     *
     * <p>
     * This operation is used by the telephony runtime when
     * the underlying call reaches a terminal state.
     * </p>
     *
     * @param callId call identifier
     * @return updated call session
     */
    CallSessionResponseDto endCallSession(
            String callId
    );

    /**
     * Updates the lifecycle status of a CallSession.
     *
     * @param callId call identifier
     * @param status new session status
     * @return updated call session
     */
    CallSessionResponseDto updateStatus(
            String callId,
            CallSessionStatus status
    );
}