package com.infinitio.aivoiceplatform.voicegateway.service.impl;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionGetService;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayMessages;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayCallContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link VoiceGatewayCallContextService}.
 *
 * <p>
 * Resolves the runtime context of a voice call from the existing
 * Call Session. The Call Session is the application-level source
 * of truth for the tenant, agent, agent version, language and
 * current Flow execution associated with the call.
 * </p>
 *
 * <p>
 * This service intentionally does not accept tenant, agent or Flow
 * identifiers from the telephony provider. Those values must come
 * from the application's persisted runtime session.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoiceGatewayCallContextServiceImpl
        implements VoiceGatewayCallContextService {

    private final CallSessionGetService callSessionGetService;

    /**
     * {@inheritDoc}
     */
    @Override
    public CallSessionResponseDto resolveCallSession(
            String callId) {

        validateCallId(
                callId
        );

        log.debug(
                "Resolving Voice Gateway call session. callId={}",
                callId
        );

        CallSessionResponseDto session =
                callSessionGetService.getCallSession(
                        callId
                );

        if (session == null) {

            log.error(
                    "Call session resolution returned null. callId={}",
                    callId
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.CALL_SESSION_NOT_FOUND
            );
        }

        log.debug(
                "Voice Gateway call session resolved. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "agentVersion={}, language={}, " +
                        "flowNodeId={}, flowExecutionPublicId={}",
                callId,
                session.getTenantId(),
                session.getAgentId(),
                session.getAgentVersion(),
                session.getLanguage(),
                session.getFlowNodeId(),
                session.getFlowExecutionPublicId()
        );

        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveTenantPublicId(
            String callId) {

        CallSessionResponseDto session =
                resolveCallSession(
                        callId
                );

        validateResolvedValue(
                session.getTenantId(),
                "tenantId"
        );

        return session.getTenantId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveAgentPublicId(
            String callId) {

        CallSessionResponseDto session =
                resolveCallSession(
                        callId
                );

        validateResolvedValue(
                session.getAgentId(),
                "agentId"
        );

        return session.getAgentId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer resolveAgentVersion(
            String callId) {

        CallSessionResponseDto session =
                resolveCallSession(
                        callId
                );

        if (session.getAgentVersion() == null) {

            log.error(
                    "Agent version is missing from Call Session. " +
                            "callId={}",
                    callId
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }

        return session.getAgentVersion();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The Call Session currently stores the Flow execution
     * information rather than a direct flowPublicId. Therefore
     * this implementation does not invent or derive a Flow ID
     * from an unrelated field.
     * </p>
     */
    @Override
    public String resolveFlowPublicId(
            String callId) {

        CallSessionResponseDto session =
                resolveCallSession(
                        callId
                );

        /*
         * The current CallSessionResponseDto does not expose a
         * flowPublicId. Do not incorrectly use flowNodeId or
         * flowExecutionPublicId as a Flow identifier.
         *
         * The Conversation/Flow runtime should continue from
         * the persisted Flow execution instead.
         */
        log.debug(
                "Direct Flow public ID is not stored in the " +
                        "current Call Session response. " +
                        "callId={}, flowNodeId={}, " +
                        "flowExecutionPublicId={}",
                callId,
                session.getFlowNodeId(),
                session.getFlowExecutionPublicId()
        );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveLanguage(
            String callId) {

        CallSessionResponseDto session =
                resolveCallSession(
                        callId
                );

        String language =
                session.getLanguage();

        if (language == null
                || language.isBlank()) {

            log.debug(
                    "Call Session has no language configured. " +
                            "callId={}",
                    callId
            );

            return null;
        }

        return language;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Validates a call identifier.
     *
     * @param callId call identifier
     */
    private void validateCallId(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            log.warn(
                    "Voice Gateway call ID is missing."
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }
    }

    /**
     * Validates a resolved runtime value.
     *
     * @param value resolved value
     * @param fieldName field name used for logging
     */
    private void validateResolvedValue(
            String value,
            String fieldName) {

        if (value == null
                || value.isBlank()) {

            log.error(
                    "Required runtime context value is missing. " +
                            "field={}",
                    fieldName
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }
    }
}