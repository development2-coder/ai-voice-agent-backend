package com.infinitio.aivoiceplatform.agent.dto.response;

import lombok.*;

/**
 * Agent Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    private String publicId;

    private String organizationPublicId;

    private String tenantPublicId;

    private String agentCode;

    private String agentName;

    private String description;

    private String welcomeMessage;

    private String language;

    private Integer isActive;

}