package com.infinitio.aivoiceplatform.callsession.dto.response;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Represents the Call Session response.
 *
 * <p>
 * The response contains all runtime identifiers required by
 * downstream conversation and telephony services.
 * </p>
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

    /**
     * Application Call public identifier.
     */
    private String callId;

    /**
     * Tenant public identifier.
     */
    private String tenantId;

    /**
     * Agent public identifier.
     */
    private String agentId;

    /**
     * Agent version used by the runtime.
     */
    private Integer agentVersion;

    /**
     * Flow public identifier assigned to the Call Session.
     *
     * <p>
     * This value is important for real-time Voice Gateway
     * startup because the telephony provider must not decide
     * which Flow should execute.
     * </p>
     */
    private String flowPublicId;

    /**
     * Current conversation turn index.
     */
    private Integer turnIndex;

    /**
     * Conversation history.
     */
    private List<CallConversationMessageResponseDto>
            conversationHistory;

    /**
     * Collected Flow slots.
     */
    private Map<String, String> collectedSlots;

    /**
     * Current Flow node identifier.
     */
    private String flowNodeId;

    /**
     * Conversation language.
     */
    private String language;

    /**
     * Current Call Session status.
     */
    private CallSessionStatus status;

    /**
     * Flow Execution public identifier.
     */
    private String flowExecutionPublicId;
}