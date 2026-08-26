package com.infinitio.aivoiceplatform.agent.dto.request;

import com.infinitio.aivoiceplatform.agent.constant.AgentConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Agent Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentRequest {

    @NotBlank(message = "Agent public id is required.")
    private String publicId;

    @NotBlank(message = "Organization is required.")
    private String organizationPublicId;

    @NotBlank(message = "Tenant is required.")
    private String tenantPublicId;

    @NotBlank(message = "Agent code is required.")
    @Size(max = AgentConstants.AGENT_CODE_MAX_LENGTH)
    private String agentCode;

    @NotBlank(message = "Agent name is required.")
    @Size(max = AgentConstants.AGENT_NAME_MAX_LENGTH)
    private String agentName;

    @Size(max = AgentConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Size(max = AgentConstants.WELCOME_MESSAGE_MAX_LENGTH)
    private String welcomeMessage;

    @Size(max = AgentConstants.LANGUAGE_MAX_LENGTH)
    private String language;

}