package com.infinitio.aivoiceplatform.callsession.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.CallConversationMessageDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallConversationMessageResponseDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;

import lombok.RequiredArgsConstructor;

/**
 * Maps call session objects between request, domain and response models.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CallSessionMapper {

    /**
     * Converts a create request into a call session.
     *
     * @param request creation request
     * @return call session
     */
    public CallSession toEntity(
            CreateCallSessionRequestDto request) {

        return CallSession.builder()
                .callId(
                        request.getCallId()
                )
                .tenantId(
                        request.getTenantId()
                )
                .agentId(
                        request.getAgentId()
                )
                .agentVersion(
                        request.getAgentVersion()
                )
                .turnIndex(0)
                .collectedSlots(
                        new HashMap<>()
                )
                .flowNodeId(
                        request.getFlowNodeId()
                )
                .language(
                        request.getLanguage()
                )
                .status(
                        CallSessionStatus.ACTIVE
                )
                .build();
    }

    /**
     * Updates a call session with the runtime state
     * returned by the Flow Execution module.
     *
     * @param callSession call session
     * @param execution flow execution result
     */
    public void updateFromExecution(
            CallSession callSession,
            FlowExecutionResult execution) {

        if (execution == null) {
            return;
        }

        callSession.setFlowExecutionPublicId(
                execution.getExecutionPublicId()
        );

        callSession.setFlowNodeId(
                execution.getCurrentNodeKey()
        );

        if (execution.getStatus() != null) {

            callSession.setStatus(
                    CallSessionStatus.valueOf(
                            execution
                                    .getStatus()
                                    .name()
                    )
            );
        }

        callSession.setCollectedSlots(
                mapCollectedSlots(
                        execution.getContext()
                )
        );
    }

    /**
     * Converts a call session into a response DTO.
     *
     * @param callSession call session
     * @return response DTO
     */
    public CallSessionResponseDto toResponse(
            CallSession callSession) {

        return CallSessionResponseDto.builder()
                .callId(
                        callSession.getCallId()
                )
                .tenantId(
                        callSession.getTenantId()
                )
                .agentId(
                        callSession.getAgentId()
                )
                .agentVersion(
                        callSession.getAgentVersion()
                )
                .flowExecutionPublicId(
                        callSession.getFlowExecutionPublicId()
                )
                .turnIndex(
                        callSession.getTurnIndex()
                )
                .collectedSlots(
                        callSession.getCollectedSlots() == null
                                ? Collections.emptyMap()
                                : new HashMap<>(
                                callSession
                                        .getCollectedSlots()
                        )
                )
                .flowNodeId(
                        callSession.getFlowNodeId()
                )
                .language(
                        callSession.getLanguage()
                )
                .status(
                        callSession.getStatus()
                )
                .build();
    }

    /**
     * Converts a create request and flow execution result
     * into a call session response.
     *
     * @param request creation request
     * @param execution flow execution result
     * @return call session response
     */
    public CallSessionResponseDto toResponse(
            CreateCallSessionRequestDto request,
            FlowExecutionResult execution) {

        Map<String, Object> context =
                execution.getContext() == null
                        ? Collections.emptyMap()
                        : execution.getContext();

        return CallSessionResponseDto.builder()
                .callId(
                        request.getCallId()
                )
                .tenantId(
                        request.getTenantId()
                )
                .agentId(
                        request.getAgentId()
                )
                .agentVersion(
                        request.getAgentVersion()
                )
                .turnIndex(0)
                .conversationHistory(
                        Collections.emptyList()
                )
                .collectedSlots(
                        mapCollectedSlots(
                                context
                        )
                )
                .flowNodeId(
                        execution.getCurrentNodeKey()
                )
                .language(
                        request.getLanguage()
                )
                .status(
                        mapStatus(
                                execution
                        )
                )
                .flowExecutionPublicId(
                        execution.getExecutionPublicId()
                )
                .build();
    }

    /**
     * Converts flow execution context into call session slots.
     *
     * @param context flow execution context
     * @return collected slots
     */
    private Map<String, String> mapCollectedSlots(
            Map<String, Object> context) {

        if (context == null
                || context.isEmpty()) {

            return Collections.emptyMap();
        }

        Map<String, String> collectedSlots =
                new HashMap<>();

        context.forEach(
                (key, value) -> {

                    if (key != null
                            && value != null) {

                        collectedSlots.put(
                                key,
                                String.valueOf(value)
                        );
                    }
                }
        );

        return collectedSlots;
    }

    /**
     * Maps flow execution status to call session status.
     *
     * @param execution flow execution result
     * @return call session status
     */
    private CallSessionStatus mapStatus(
            FlowExecutionResult execution) {

        if (execution == null
                || execution.getStatus() == null) {

            return CallSessionStatus.ACTIVE;
        }

        return CallSessionStatus.valueOf(
                execution
                        .getStatus()
                        .name()
        );
    }

    /**
     * Maps conversation history messages.
     *
     * @param messages conversation messages
     * @return response messages
     */
    private List<CallConversationMessageResponseDto>
    mapConversationHistory(
            List<CallConversationMessageDto> messages) {

        if (messages == null
                || messages.isEmpty()) {

            return Collections.emptyList();
        }

        return messages.stream()
                .map(message ->
                        CallConversationMessageResponseDto
                                .builder()
                                .role(
                                        message.getRole()
                                )
                                .text(
                                        message.getText()
                                )
                                .timestamp(
                                        message.getTimestamp()
                                )
                                .build()
                )
                .toList();
    }
}