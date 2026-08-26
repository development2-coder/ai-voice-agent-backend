package com.infinitio.aivoiceplatform.callsession.service.impl;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateFlowStateRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionFlowService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements call session flow state business logic.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionFlowServiceImpl
        implements CallSessionFlowService {

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public CallSessionResponseDto updateFlowState(
            String callId,
            UpdateFlowStateRequestDto request) {

        log.info(
                "Updating call session flow state. callId={}, flowNodeId={}",
                callId,
                request != null
                        ? request.getFlowNodeId()
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
                                    "Call session not found while updating flow state. callId={}",
                                    callId
                            );

                            return new ResourceNotFoundException(
                                    CallSessionMessages
                                            .CALL_SESSION_NOT_FOUND
                            );
                        });

        callSession.setFlowNodeId(
                request.getFlowNodeId()
        );

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "Call session flow state updated successfully. callId={}, flowNodeId={}",
                callId,
                savedCallSession.getFlowNodeId()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    private void validateRequest(
            String callId,
            UpdateFlowStateRequestDto request) {

        if (callId == null
                || callId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }

        if (request == null
                || request.getFlowNodeId() == null
                || request.getFlowNodeId().isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .FLOW_NODE_ID_REQUIRED
            );
        }
    }
}