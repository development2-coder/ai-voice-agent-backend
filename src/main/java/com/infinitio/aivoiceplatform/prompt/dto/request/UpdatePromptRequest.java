package com.infinitio.aivoiceplatform.prompt.dto.request;

import com.infinitio.aivoiceplatform.prompt.constant.PromptConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Prompt Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePromptRequest {

    @NotBlank(message = "Prompt public id is required.")
    private String publicId;

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "Prompt code is required.")
    @Size(max = PromptConstants.PROMPT_CODE_MAX_LENGTH)
    private String promptCode;

    @NotBlank(message = "Prompt name is required.")
    @Size(max = PromptConstants.PROMPT_NAME_MAX_LENGTH)
    private String promptName;

    @Size(max = PromptConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotBlank(message = "System prompt is required.")
    private String systemPrompt;

    @Size(max = PromptConstants.PROMPT_TYPE_MAX_LENGTH)
    private String promptType;

    @Size(max = PromptConstants.VERSION_MAX_LENGTH)
    private String version;

    @NotNull(message = "Default prompt status is required.")
    private Boolean defaultPrompt;
}