package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Handles call session creation operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionCreateService {

    /**
     * Creates a call session using the authenticated user.
     *
     * @param request call session creation request
     * @return created call session
     */
    CallSessionResponseDto createCallSession(
            CreateCallSessionRequestDto request
    );

    /**
     * Creates a call session using a supplied audit user.
     *
     * <p>
     * This method is intended for system-driven workflows
     * such as AI Dialer or scheduler execution.
     * </p>
     *
     * @param request call session creation request
     * @param createdBy audit user ID
     * @return created call session
     */
    CallSessionResponseDto createCallSession(
            CreateCallSessionRequestDto request,
            Long createdBy
    );
}