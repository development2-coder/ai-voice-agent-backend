package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

import java.util.Map;

/**
 * Handles runtime Flow Execution for Call Sessions.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionFlowRuntimeService {

    /**
     * Starts the Flow associated with a Call Session.
     *
     * @param callId platform Call public ID
     * @param flowPublicId Flow public ID
     * @param language session language
     * @param context initial runtime context
     * @return updated Call Session
     */
    CallSessionResponseDto startFlow(
            String callId,
            String flowPublicId,
            String language,
            Map<String, Object> context
    );
}