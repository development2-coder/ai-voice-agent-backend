package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Handles call session retrieval operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionGetService {

    /**
     * Retrieves a call session by call identifier.
     *
     * @param callId call identifier
     * @return call session
     */
    CallSessionResponseDto getCallSession(String callId);
}