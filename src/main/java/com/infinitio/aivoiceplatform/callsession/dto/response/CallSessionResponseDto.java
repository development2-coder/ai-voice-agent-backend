package com.infinitio.aivoiceplatform.callsession.dto.response;

import java.util.List;
import java.util.Map;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the call session response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSessionResponseDto {

    private String callId;

    private String tenantId;

    private String agentId;

    private Integer agentVersion;

    private Integer turnIndex;

    private List<CallConversationMessageResponseDto> conversationHistory;

    private Map<String, String> collectedSlots;

    private String flowNodeId;

    private String language;

    private CallSessionStatus status;

    private String flowExecutionPublicId;
}