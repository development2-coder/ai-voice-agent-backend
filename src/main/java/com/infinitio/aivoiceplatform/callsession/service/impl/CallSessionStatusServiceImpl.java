package com.infinitio.aivoiceplatform.callsession.service.impl;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionStatusRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionStatusService;
import com.infinitio.aivoiceplatform.callsession.storage.ConversationStorageService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements call session lifecycle status business logic.
 *
 * <p>
 * When a call reaches ENDED status, its active JSONL conversation
 * is archived into a GZIP-compressed JSONL file.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionStatusServiceImpl
        implements CallSessionStatusService {

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    private final ConversationStorageService
            conversationStorageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public CallSessionResponseDto updateStatus(
            String callId,
            UpdateCallSessionStatusRequestDto request) {

        log.info(
                "Updating call session status. callId={}, status={}",
                callId,
                request != null
                        ? request.getStatus()
                        : null
        );

        validateRequest(
                callId,
                request
        );

        CallSession callSession =
                callSessionRepository
                        .findByCallId(callId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Call session not found while updating status. callId={}",
                                    callId
                            );

                            return new ResourceNotFoundException(
                                    CallSessionMessages
                                            .CALL_SESSION_NOT_FOUND
                            );
                        });

        log.info(
                "Call session status transition. " +
                        "callId={}, oldStatus={}, newStatus={}",
                callId,
                callSession.getStatus(),
                request.getStatus()
        );

        if (CallSessionStatus.ENDED.equals(
                request.getStatus()
        )) {

            String archivedStorageKey =
                    conversationStorageService
                            .archiveConversation(
                                    callSession
                                            .getConversationStorageKey()
                            );

            if (archivedStorageKey != null) {

                callSession.setConversationStorageKey(
                        archivedStorageKey
                );
            }
        }

        callSession.setStatus(
                request.getStatus()
        );

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "Call session status updated successfully. " +
                        "callId={}, status={}, storageKey={}",
                callId,
                savedCallSession.getStatus(),
                savedCallSession.getConversationStorageKey()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    /**
     * Validates status update request.
     *
     * @param callId call identifier
     * @param request status request
     */
    private void validateRequest(
            String callId,
            UpdateCallSessionStatusRequestDto request) {

        if (callId == null
                || callId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }

        if (request == null
                || request.getStatus() == null) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_SESSION_STATUS_REQUIRED
            );
        }
    }
}