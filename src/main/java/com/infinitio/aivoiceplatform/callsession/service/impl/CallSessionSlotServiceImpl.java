package com.infinitio.aivoiceplatform.callsession.service.impl;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCollectedSlotRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionSlotService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements collected slot business logic.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionSlotServiceImpl
        implements CallSessionSlotService {

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public CallSessionResponseDto updateCollectedSlot(
            String callId,
            UpdateCollectedSlotRequestDto request) {

        log.info(
                "Updating collected slot. callId={}, slotName={}",
                callId,
                request != null
                        ? request.getSlotName()
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
                                    "Call session not found while updating slot. callId={}",
                                    callId
                            );

                            return new ResourceNotFoundException(
                                    CallSessionMessages
                                            .CALL_SESSION_NOT_FOUND
                            );
                        });

        callSession.addCollectedSlot(
                request.getSlotName(),
                request.getValue()
        );

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "Collected slot updated successfully. callId={}, slotName={}",
                callId,
                request.getSlotName()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    private void validateRequest(
            String callId,
            UpdateCollectedSlotRequestDto request) {

        if (callId == null
                || callId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }

        if (request == null
                || request.getSlotName() == null
                || request.getSlotName().isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages.COLLECTED_SLOT_NAME_REQUIRED
            );
        }
    }
}