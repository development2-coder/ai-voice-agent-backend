package com.infinitio.aivoiceplatform.agent.dto.request;

import com.infinitio.aivoiceplatform.agent.constant.AgentConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for creating a copy of an existing Agent.
 *
 * <p>
 * The source Agent is identified by the path variable. Only the
 * new Agent identity is supplied in this request. The remaining
 * Agent configuration and Flow Builder data are copied from the
 * source Agent.
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
public class CopyAgentRequest {

    /**
     * Agent code for the copied Agent.
     */
    @NotBlank(message = "Agent code is required.")
    @Size(max = AgentConstants.AGENT_CODE_MAX_LENGTH)
    private String agentCode;

    /**
     * Agent name for the copied Agent.
     */
    @NotBlank(message = "Agent name is required.")
    @Size(max = AgentConstants.AGENT_NAME_MAX_LENGTH)
    private String agentName;

    /**
     * Optional description for the copied Agent.
     *
     * <p>
     * If omitted, the source Agent description is retained.
     * </p>
     */
    @Size(max = AgentConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}