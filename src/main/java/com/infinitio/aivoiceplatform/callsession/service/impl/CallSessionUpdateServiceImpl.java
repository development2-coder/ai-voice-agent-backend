package com.infinitio.aivoiceplatform.callsession.service.impl;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionUpdateService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implements general CallSession update business logic.
 *
 * <p>
 * This service handles normal session updates as well as
 * terminal lifecycle transitions triggered by the telephony
 * runtime.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionUpdateServiceImpl
        implements CallSessionUpdateService {

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    /**
     * Updates general CallSession information.
     *
     * @param callId call identifier
     * @param request update request
     * @return updated CallSession
     */
    @Override
    public CallSessionResponseDto updateCallSession(
            String callId,
            UpdateCallSessionRequestDto request) {

        log.info(
                "Updating CallSession. callId={}",
                callId
        );

        validateRequest(
                callId,
                request
        );

        CallSession callSession =
                findCallSession(
                        callId
                );

        /*
         * Do not modify an already ended session through the
         * general update operation.
         */
        if (CallSessionStatus.ENDED
                .equals(
                        callSession.getStatus()
                )) {

            log.warn(
                    "CallSession is already ended. "
                            + "Ignoring general update. callId={}",
                    callId
            );

            return callSessionMapper.toResponse(
                    callSession
            );
        }

        if (request.getAgentVersion() != null) {

            callSession.setAgentVersion(
                    request.getAgentVersion()
            );
        }

        if (request.getLanguage() != null
                && !request.getLanguage().isBlank()) {

            callSession.setLanguage(
                    request.getLanguage()
            );
        }

        if (request.getFlowExecutionPublicId() != null
                && !request
                .getFlowExecutionPublicId()
                .isBlank()) {

            callSession.setFlowExecutionPublicId(
                    request
                            .getFlowExecutionPublicId()
            );
        }

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "CallSession updated successfully. "
                        + "callId={}, status={}",
                callId,
                savedCallSession.getStatus()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    /**
     * Ends a CallSession.
     *
     * <p>
     * This operation is idempotent. If the telephony provider
     * sends the same terminal webhook more than once, an already
     * ended CallSession is simply returned without another
     * persistence operation.
     * </p>
     *
     * @param callId call identifier
     * @return ended CallSession
     */
    @Override
    public CallSessionResponseDto endCallSession(
            String callId) {

        log.info(
                "Ending CallSession. callId={}",
                callId
        );

        validateCallId(
                callId
        );

        CallSession callSession =
                findCallSession(
                        callId
                );

        /*
         * ---------------------------------------------------------
         * IDEMPOTENCY
         * ---------------------------------------------------------
         *
         * Exotel/provider webhooks can be retried.
         *
         * If the session has already been ended, do not modify
         * it again.
         */
        if (CallSessionStatus.ENDED
                .equals(
                        callSession.getStatus()
                )) {

            log.info(
                    "CallSession is already ENDED. "
                            + "Skipping duplicate termination. "
                            + "callId={}",
                    callId
            );

            return callSessionMapper.toResponse(
                    callSession
            );
        }

        CallSessionStatus previousStatus =
                callSession.getStatus();

        callSession.setStatus(
                CallSessionStatus.ENDED
        );

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "CallSession ended successfully. "
                        + "callId={}, previousStatus={}, "
                        + "newStatus={}",
                callId,
                previousStatus,
                savedCallSession.getStatus()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    /**
     * Updates the lifecycle status of a CallSession.
     *
     * <p>
     * This method is intentionally restricted to the existing
     * CallSessionStatus values.
     * </p>
     *
     * @param callId call identifier
     * @param status new status
     * @return updated CallSession
     */
    @Override
    public CallSessionResponseDto updateStatus(
            String callId,
            CallSessionStatus status) {

        log.info(
                "Updating CallSession status. "
                        + "callId={}, status={}",
                callId,
                status
        );

        validateCallId(
                callId
        );

        if (status == null) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_SESSION_STATUS_REQUIRED
            );
        }

        CallSession callSession =
                findCallSession(
                        callId
                );

        CallSessionStatus previousStatus =
                callSession.getStatus();

        /*
         * Terminal state protection.
         *
         * Once a CallSession is ENDED, it must not be moved
         * back to ACTIVE or TRANSFERRING.
         */
        if (CallSessionStatus.ENDED
                .equals(
                        previousStatus
                )
                && !CallSessionStatus.ENDED
                .equals(
                        status
                )) {

            log.warn(
                    "Ignoring invalid CallSession status "
                            + "transition from ENDED. "
                            + "callId={}, requestedStatus={}",
                    callId,
                    status
            );

            return callSessionMapper.toResponse(
                    callSession
            );
        }

        /*
         * Same status is idempotent.
         */
        if (status.equals(
                previousStatus
        )) {

            log.debug(
                    "CallSession already has requested status. "
                            + "callId={}, status={}",
                    callId,
                    status
            );

            return callSessionMapper.toResponse(
                    callSession
            );
        }

        callSession.setStatus(
                status
        );

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "CallSession status updated successfully. "
                        + "callId={}, previousStatus={}, "
                        + "newStatus={}",
                callId,
                previousStatus,
                savedCallSession.getStatus()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    /**
     * Finds a CallSession by Call ID.
     *
     * @param callId call identifier
     * @return CallSession
     */
    private CallSession findCallSession(
            String callId) {

        return callSessionRepository
                .findByCallId(
                        callId
                )
                .orElseThrow(() -> {

                    log.warn(
                            "CallSession not found. "
                                    + "callId={}",
                            callId
                    );

                    return new ResourceNotFoundException(
                            CallSessionMessages
                                    .CALL_SESSION_NOT_FOUND
                    );
                });
    }

    /**
     * Validates a CallSession update request.
     *
     * @param callId call identifier
     * @param request update request
     */
    private void validateRequest(
            String callId,
            UpdateCallSessionRequestDto request) {

        validateCallId(
                callId
        );

        if (request == null) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_SESSION_UPDATE_REQUEST_REQUIRED
            );
        }

        if (request.getAgentVersion() != null
                && request.getAgentVersion() <= 0) {

            throw new BadRequestException(
                    CallSessionMessages
                            .AGENT_VERSION_INVALID
            );
        }
    }

    /**
     * Validates Call ID.
     *
     * @param callId call identifier
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