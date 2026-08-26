package com.infinitio.aivoiceplatform.prompt.dto.response;

import lombok.*;

/**
 * Prompt Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {

    private String publicId;

    private String agentPublicId;

    private String promptCode;

    private String promptName;

    private String description;

    private String systemPrompt;

    private String promptType;

    private String version;

    private Boolean defaultPrompt;

    private Integer isActive;
}