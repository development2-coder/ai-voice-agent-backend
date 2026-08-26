package com.infinitio.aivoiceplatform.callsession.service.impl;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionFlowRuntimeService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.dto.request.StartFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

/**
 * Runtime integration between Call Session
 * and Flow Execution.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallSessionFlowRuntimeServiceImpl
        implements CallSessionFlowRuntimeService {

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    private final FlowExecutionService
            flowExecutionService;

    @Override
    public CallSessionResponseDto startFlow(
            String callId,
            String flowPublicId,
            String language,
            Map<String, Object> context) {

        validateRequest(
                callId,
                flowPublicId
        );

        log.info(
                "Starting Flow for Call Session. "
                        + "callId={}, flowPublicId={}",
                callId,
                flowPublicId
        );

        CallSession callSession =
                callSessionRepository
                        .findByCallId(
                                callId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        CallSessionMessages
                                                .CALL_SESSION_NOT_FOUND
                                )
                        );

        /*
         * Prevent duplicate Flow Execution.
         */
        if (callSession
                .getFlowExecutionPublicId() != null
                && !callSession
                .getFlowExecutionPublicId()
                .isBlank()) {

            log.info(
                    "Call Session already has Flow Execution. "
                            + "callId={}, execution={}",
                    callId,
                    callSession
                            .getFlowExecutionPublicId()
            );

            return callSessionMapper.toResponse(
                    callSession
            );
        }

        StartFlowExecutionRequest request =
                StartFlowExecutionRequest.builder()
                        .flowPublicId(
                                flowPublicId
                        )
                        .callPublicId(
                                callId
                        )
                        .context(
                                context == null
                                        ? Collections.emptyMap()
                                        : context
                        )
                        .build();

        FlowExecutionResult execution =
                flowExecutionService.start(
                        request
                );

        if (execution == null) {

            throw new IllegalStateException(
                    "Flow execution did not return a result."
            );
        }

        /*
         * Existing mapper owns the mapping between
         * FlowExecutionResult and CallSession.
         */
        callSessionMapper.updateFromExecution(
                callSession,
                execution
        );

        if (language != null
                && !language.isBlank()) {

            callSession.setLanguage(
                    language
            );
        }

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "Flow started successfully. "
                        + "callId={}, execution={}, node={}",
                callId,
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    private void validateRequest(
            String callId,
            String flowPublicId) {

        if (callId == null
                || callId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }

        if (flowPublicId == null
                || flowPublicId.isBlank()) {

            throw new BadRequestException(
                    "Flow public ID is required."
            );
        }
    }
}