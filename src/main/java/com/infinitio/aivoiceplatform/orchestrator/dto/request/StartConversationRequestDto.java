package com.infinitio.aivoiceplatform.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Request DTO used to start a conversation runtime.
 *
 * <p>
 * Contains the identifiers required to associate the
 * conversation with a tenant, agent, flow and call.
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
public class StartConversationRequestDto {

    /**
     * Unique public identifier of the call.
     */
    @NotBlank
    private String callId;

    /**
     * Unique public identifier of the tenant.
     */
    @NotBlank
    private String tenantId;

    /**
     * Unique public identifier of the agent.
     */
    @NotBlank
    private String agentId;

    /**
     * Unique public identifier of the flow.
     */
    @NotBlank
    private String flowPublicId;

    /**
     * Version of the agent used for this call.
     */
    private Integer agentVersion;

    /**
     * Conversation language.
     */
    private String language;

    /**
     * Existing conversation public identifier.
     *
     * <p>
     * This may be null when a new conversation is started.
     * </p>
     */
    private String conversationPublicId;

    /**
     * Initial runtime context supplied to the flow.
     */
    private Map<String, Object> context;
}