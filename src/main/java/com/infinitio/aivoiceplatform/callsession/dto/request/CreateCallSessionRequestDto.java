package com.infinitio.aivoiceplatform.callsession.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request used to create the runtime state for a call.
 *
 * <p>
 * The request identifies the call, tenant, agent and flow
 * that will be used for the runtime execution.
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
public class CreateCallSessionRequestDto {

    /**
     * Public identifier of the call.
     */
    @NotBlank
    private String callId;

    /**
     * Public identifier of the tenant.
     */
    @NotBlank
    private String tenantId;

    /**
     * Public identifier of the agent.
     */
    @NotBlank
    private String agentId;

    /**
     * Version of the agent being used.
     */
    @NotNull
    @Positive
    private Integer agentVersion;

    /**
     * Public identifier of the flow to execute.
     */
    @NotBlank
    private String flowPublicId;

    /**
     * Current flow node.
     *
     * <p>
     * This field is retained for API compatibility.
     * The actual current node for a newly created flow
     * execution is determined from the START node by the
     * Flow Execution module.
     * </p>
     */
    private String flowNodeId;

    /**
     * Language selected for the call.
     */
    private String language;
}