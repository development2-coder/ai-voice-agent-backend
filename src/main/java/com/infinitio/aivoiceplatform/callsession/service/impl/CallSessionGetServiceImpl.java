package com.infinitio.aivoiceplatform.callsession.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionGetService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements call-session retrieval business logic.
 *
 * <p>
 * Call-session metadata is loaded from MySQL while conversation
 * history is loaded from the configured local conversation storage.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionGetServiceImpl
        implements CallSessionGetService {

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CallSessionResponseDto getCallSession(
            String callId) {

        log.info(
                "Fetching call session. callId={}",
                callId
        );

        validateCallId(
                callId
        );

        CallSession callSession =
                callSessionRepository
                        .findByCallId(callId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Call session not found. callId={}",
                                    callId
                            );

                            return new ResourceNotFoundException(
                                    CallSessionMessages
                                            .CALL_SESSION_NOT_FOUND
                            );
                        });

        log.info(
                "Call session fetched successfully. callId={}",
                callId
        );

        return callSessionMapper.toResponse(
                callSession
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