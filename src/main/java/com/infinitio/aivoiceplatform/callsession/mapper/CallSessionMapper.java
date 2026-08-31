package com.infinitio.aivoiceplatform.callsession.mapper;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps Call Session objects between request,
 * domain and response models.
 *
 * <p>
 * Flow public ID belongs to the persistent Call Session
 * runtime context because the real-time Voice Gateway must
 * recover the exact Flow selected when the call was created.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CallSessionMapper {

    /**
     * Converts a create request into a Call Session entity.
     *
     * @param request Call Session creation request
     * @return Call Session entity
     */
    public CallSession toEntity(
            CreateCallSessionRequestDto request) {

        if (request == null) {

            return null;
        }

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
                .flowPublicId(
                        request.getFlowPublicId()
                )
                .turnIndex(
                        0
                )
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
     * Updates a Call Session using the latest Flow Execution state.
     *
     * @param callSession Call Session
     * @param execution Flow Execution result
     */
    public void updateFromExecution(
            CallSession callSession,
            FlowExecutionResult execution) {

        if (callSession == null
                || execution == null) {

            return;
        }

        if (execution.getExecutionPublicId() != null) {

            callSession.setFlowExecutionPublicId(
                    execution.getExecutionPublicId()
            );
        }

        if (execution.getCurrentNodeKey() != null
                && !execution.getCurrentNodeKey().isBlank()) {

            callSession.setFlowNodeId(
                    execution.getCurrentNodeKey()
            );
        }

        if (execution.getStatus() != null) {

            try {

                callSession.setStatus(
                        CallSessionStatus.valueOf(
                                execution
                                        .getStatus()
                                        .name()
                        )
                );

            } catch (IllegalArgumentException exception) {

                /*
                 * Keep the existing Call Session status when
                 * the Flow status has no direct equivalent.
                 */
            }
        }

        callSession.setCollectedSlots(
                mapCollectedSlots(
                        execution.getContext()
                )
        );
    }

    /**
     * Converts a Call Session entity into a response DTO.
     *
     * @param callSession Call Session entity
     * @return Call Session response
     */
    public CallSessionResponseDto toResponse(
            CallSession callSession) {

        if (callSession == null) {

            return null;
        }

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
                .flowPublicId(
                        callSession.getFlowPublicId()
                )
                .flowExecutionPublicId(
                        callSession.getFlowExecutionPublicId()
                )
                .turnIndex(
                        callSession.getTurnIndex()
                )
                .conversationHistory(
                        Collections.emptyList()
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
     * Converts a Call Session creation request and Flow
     * Execution result into a response DTO.
     *
     * @param request Call Session creation request
     * @param execution Flow Execution result
     * @return Call Session response
     */
    public CallSessionResponseDto toResponse(
            CreateCallSessionRequestDto request,
            FlowExecutionResult execution) {

        if (request == null) {

            return null;
        }

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
                .flowPublicId(
                        request.getFlowPublicId()
                )
                .turnIndex(
                        0
                )
                .conversationHistory(
                        Collections.emptyList()
                )
                .collectedSlots(
                        execution == null
                                ? Collections.emptyMap()
                                : mapCollectedSlots(
                                execution.getContext()
                        )
                )
                .flowNodeId(
                        execution == null
                                ? request.getFlowNodeId()
                                : execution.getCurrentNodeKey()
                )
                .language(
                        request.getLanguage()
                )
                .status(
                        execution == null
                                ? CallSessionStatus.ACTIVE
                                : mapStatus(
                                execution
                        )
                )
                .flowExecutionPublicId(
                        execution == null
                                ? null
                                : execution.getExecutionPublicId()
                )
                .build();
    }

    /**
     * Converts Flow Execution context into collected slots.
     *
     * @param context Flow Execution context
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

                    if (key == null
                            || value == null) {

                        return;
                    }

                    if (value instanceof String) {

                        collectedSlots.put(
                                key,
                                (String) value
                        );

                    } else if (value instanceof Number
                            || value instanceof Boolean) {

                        collectedSlots.put(
                                key,
                                String.valueOf(
                                        value
                                )
                        );
                    }
                }
        );

        return collectedSlots;
    }

    /**
     * Maps Flow Execution status to Call Session status.
     *
     * @param execution Flow Execution result
     * @return Call Session status
     */
    private CallSessionStatus mapStatus(
            FlowExecutionResult execution) {

        if (execution == null
                || execution.getStatus() == null) {

            return CallSessionStatus.ACTIVE;
        }

        try {

            return CallSessionStatus.valueOf(
                    execution
                            .getStatus()
                            .name()
            );

        } catch (IllegalArgumentException exception) {

            return CallSessionStatus.ACTIVE;
        }
    }
}