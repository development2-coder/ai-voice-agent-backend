package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Starts and initializes the runtime Call Session
 * for an active platform Call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionRuntimeService {

    /**
     * Starts a runtime Call Session using the
     * selected Agent and Flow configuration.
     *
     * @param callPublicId platform Call public identifier
     * @param tenantPublicId tenant public identifier
     * @param agentPublicId agent public identifier
     * @param agentVersion runtime configuration version
     * @param flowPublicId flow public identifier
     * @param language session language
     * @param createdBy audit user ID
     * @return created Call Session
     */
    CallSessionResponseDto startSession(
            String callPublicId,
            String tenantPublicId,
            String agentPublicId,
            Integer agentVersion,
            String flowPublicId,
            String language,
            Long createdBy
    );
}