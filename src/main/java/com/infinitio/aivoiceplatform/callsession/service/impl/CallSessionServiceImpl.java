package com.infinitio.aivoiceplatform.callsession.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.dto.request.AddConversationMessageRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionStatusRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCollectedSlotRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateFlowStateRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionConversationService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionCreateService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionFlowService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionGetService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionSlotService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionStatusService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionUpdateService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade implementation for call-session operations.
 *
 * <p>
 * This class coordinates the individual call-session services.
 * Business logic remains inside the respective specialized services.
 * </p>
 *
 * <p>
 * Call-session state is persisted in MySQL.
 * Redis is not used as the source of truth for call sessions.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallSessionServiceImpl
        implements CallSessionService {

    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    private static final Integer INACTIVE = 0;

    private final CallSessionCreateService
            callSessionCreateService;

    private final CallSessionGetService
            callSessionGetService;

    private final CallSessionUpdateService
            callSessionUpdateService;

    private final CallSessionConversationService
            callSessionConversationService;

    private final CallSessionFlowService
            callSessionFlowService;

    private final CallSessionSlotService
            callSessionSlotService;

    private final CallSessionStatusService
            callSessionStatusService;

    private final CallSessionRepository
            callSessionRepository;

    /**
     * Creates a new call session.
     *
     * @param request call-session creation request
     * @return created call-session response
     */
    @Override
    public CallSessionResponseDto createCallSession(
            CreateCallSessionRequestDto request) {

        log.info(
                "Creating call session through facade. callId={}",
                request != null
                        ? request.getCallId()
                        : null
        );

        return callSessionCreateService
                .createCallSession(
                        request
                );
    }

    /**
     * Retrieves a call session by call identifier.
     *
     * @param callId public call identifier
     * @return call-session response
     */
    @Override
    @Transactional(readOnly = true)
    public CallSessionResponseDto getCallSession(
            String callId) {

        log.debug(
                "Getting call session through facade. callId={}",
                callId
        );

        return callSessionGetService
                .getCallSession(
                        callId
                );
    }

    /**
     * Updates general call-session information.
     *
     * @param callId public call identifier
     * @param request update request
     * @return updated call-session response
     */
    @Override
    public CallSessionResponseDto updateCallSession(
            String callId,
            UpdateCallSessionRequestDto request) {

        log.info(
                "Updating call session through facade. callId={}",
                callId
        );

        return callSessionUpdateService
                .updateCallSession(
                        callId,
                        request
                );
    }

    /**
     * Adds a conversation message to a call session.
     *
     * @param callId public call identifier
     * @param request conversation message request
     * @return updated call-session response
     */
    @Override
    public CallSessionResponseDto addConversationMessage(
            String callId,
            AddConversationMessageRequestDto request) {

        log.info(
                "Adding conversation message through facade. callId={}",
                callId
        );

        return callSessionConversationService
                .addConversationMessage(
                        callId,
                        request
                );
    }

    /**
     * Updates the current flow execution state.
     *
     * @param callId public call identifier
     * @param request flow state request
     * @return updated call-session response
     */
    @Override
    public CallSessionResponseDto updateFlowState(
            String callId,
            UpdateFlowStateRequestDto request) {

        log.info(
                "Updating flow state through facade. callId={}",
                callId
        );

        return callSessionFlowService
                .updateFlowState(
                        callId,
                        request
                );
    }

    /**
     * Updates a collected slot.
     *
     * @param callId public call identifier
     * @param request collected-slot request
     * @return updated call-session response
     */
    @Override
    public CallSessionResponseDto updateCollectedSlot(
            String callId,
            UpdateCollectedSlotRequestDto request) {

        log.info(
                "Updating collected slot through facade. callId={}",
                callId
        );

        return callSessionSlotService
                .updateCollectedSlot(
                        callId,
                        request
                );
    }

    /**
     * Updates the call-session lifecycle status.
     *
     * @param callId public call identifier
     * @param request status request
     * @return updated call-session response
     */
    @Override
    public CallSessionResponseDto updateStatus(
            String callId,
            UpdateCallSessionStatusRequestDto request) {

        log.info(
                "Updating call session status through facade. callId={}",
                callId
        );

        return callSessionStatusService
                .updateStatus(
                        callId,
                        request
                );
    }

    /**
     * Soft deletes a call session.
     *
     * <p>
     * The call-session record remains in MySQL.
     * It is marked as deleted and inactive instead of
     * physically removing the database record.
     * </p>
     *
     * @param callId public call identifier
     */
    @Override
    public void deleteCallSession(
            String callId) {

        log.info(
                "Deleting call session through facade. callId={}",
                callId
        );

        validateCallId(
                callId
        );

        CallSession callSession =
                callSessionRepository
                        .findByCallIdAndIsDeleted(
                                callId,
                                NOT_DELETED
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Call session not found for deletion. callId={}",
                                    callId
                            );

                            return new ResourceNotFoundException(
                                    CallSessionMessages
                                            .CALL_SESSION_NOT_FOUND
                            );
                        });

        callSession.setIsDeleted(
                DELETED
        );

        callSession.setIsActive(
                INACTIVE
        );

        callSessionRepository.save(
                callSession
        );

        log.info(
                "Call session deleted successfully. callId={}",
                callId
        );
    }

    /**
     * Validates the call identifier.
     *
     * @param callId public call identifier
     */
    private void validateCallId(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }
    }
}