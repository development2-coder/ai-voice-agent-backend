package com.infinitio.aivoiceplatform.voicegateway.service;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Resolves application runtime context for a Voice Gateway call.
 *
 * <p>
 * The Voice Gateway receives provider-level information such as
 * the provider call identifier and stream identifier. The actual
 * tenant, agent and Flow configuration must be resolved from
 * the application's existing Call Session.
 * </p>
 *
 * <p>
 * This service keeps that resolution logic outside the WebSocket
 * and Voice Gateway transport layer.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface VoiceGatewayCallContextService {

    /**
     * Resolves the Call Session associated with the supplied
     * platform call identifier.
     *
     * @param callId platform Call public identifier
     * @return associated Call Session
     */
    CallSessionResponseDto resolveCallSession(
            String callId
    );

    /**
     * Resolves the tenant public identifier associated with
     * the call.
     *
     * @param callId platform Call public identifier
     * @return tenant public identifier
     */
    String resolveTenantPublicId(
            String callId
    );

    /**
     * Resolves the agent public identifier associated with
     * the call.
     *
     * @param callId platform Call public identifier
     * @return agent public identifier
     */
    String resolveAgentPublicId(
            String callId
    );

    /**
     * Resolves the agent configuration version associated
     * with the call.
     *
     * @param callId platform Call public identifier
     * @return agent version
     */
    Integer resolveAgentVersion(
            String callId
    );

    /**
     * Resolves the Flow public identifier associated with
     * the call.
     *
     * @param callId platform Call public identifier
     * @return Flow public identifier
     */
    String resolveFlowPublicId(
            String callId
    );

    /**
     * Resolves the conversation language associated with
     * the call.
     *
     * @param callId platform Call public identifier
     * @return conversation language
     */
    String resolveLanguage(
            String callId
    );
}