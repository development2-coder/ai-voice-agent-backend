package com.infinitio.aivoiceplatform.callsession.service.impl;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionFlowLinkService;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionFlowLinkServiceImpl
        implements CallSessionFlowLinkService {

    private final CallSessionRepository callSessionRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void linkExecution(
            String callId,
            FlowExecutionResult execution) {

        if (callId == null
                || callId.isBlank()
                || execution == null
                || execution.getExecutionPublicId() == null
                || execution.getExecutionPublicId().isBlank()) {

            throw new IllegalArgumentException(
                    "Call ID and Flow Execution are required."
            );
        }

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

        callSession.setFlowExecutionPublicId(
                execution.getExecutionPublicId()
        );

        if (execution.getCurrentNodeKey() != null
                && !execution.getCurrentNodeKey().isBlank()) {

            callSession.setFlowNodeId(
                    execution.getCurrentNodeKey()
            );
        }

        /*
         * Do not change CallSession status here.
         *
         * FlowExecutionStatus.RUNNING does not map directly to
         * CallSessionStatus.
         */
        callSessionRepository.saveAndFlush(
                callSession
        );

        log.info(
                "Flow Execution linked to Call Session and committed. " +
                        "callId={}, executionPublicId={}, node={}",
                callId,
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey()
        );
    }
}