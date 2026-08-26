package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.request.AddConversationMessageRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionStatusRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCollectedSlotRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateFlowStateRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Service facade for call-session operations.
 *
 * <p>
 * This service coordinates the individual call-session services.
 * Persistent call-session state is stored in MySQL.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionService {

    /**
     * Creates a new call session.
     *
     * @param request call-session creation request
     * @return created call-session response
     */
    CallSessionResponseDto createCallSession(
            CreateCallSessionRequestDto request
    );

    /**
     * Retrieves a call session by call identifier.
     *
     * @param callId public call identifier
     * @return call-session response
     */
    CallSessionResponseDto getCallSession(
            String callId
    );

    /**
     * Updates general call-session information.
     *
     * @param callId public call identifier
     * @param request update request
     * @return updated call-session response
     */
    CallSessionResponseDto updateCallSession(
            String callId,
            UpdateCallSessionRequestDto request
    );

    /**
     * Adds a conversation message to a call session.
     *
     * @param callId public call identifier
     * @param request conversation message request
     * @return updated call-session response
     */
    CallSessionResponseDto addConversationMessage(
            String callId,
            AddConversationMessageRequestDto request
    );

    /**
     * Updates the current flow execution state.
     *
     * @param callId public call identifier
     * @param request flow-state update request
     * @return updated call-session response
     */
    CallSessionResponseDto updateFlowState(
            String callId,
            UpdateFlowStateRequestDto request
    );

    /**
     * Updates a collected slot.
     *
     * @param callId public call identifier
     * @param request collected-slot update request
     * @return updated call-session response
     */
    CallSessionResponseDto updateCollectedSlot(
            String callId,
            UpdateCollectedSlotRequestDto request
    );

    /**
     * Updates the call-session status.
     *
     * @param callId public call identifier
     * @param request status update request
     * @return updated call-session response
     */
    CallSessionResponseDto updateStatus(
            String callId,
            UpdateCallSessionStatusRequestDto request
    );

    /**
     * Soft deletes a call session.
     *
     * <p>
     * The call-session record remains in MySQL and is marked
     * as deleted and inactive.
     * </p>
     *
     * @param callId public call identifier
     */
    void deleteCallSession(
            String callId
    );
}