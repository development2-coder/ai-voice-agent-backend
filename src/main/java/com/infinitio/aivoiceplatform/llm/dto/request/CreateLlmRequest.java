package com.infinitio.aivoiceplatform.llm.dto.request;

import com.infinitio.aivoiceplatform.llm.constant.LlmConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Create LLM Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLlmRequest {

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "LLM code is required.")
    @Size(max = LlmConstants.LLM_CODE_MAX_LENGTH)
    private String llmCode;

    @NotBlank(message = "LLM name is required.")
    @Size(max = LlmConstants.LLM_NAME_MAX_LENGTH)
    private String llmName;

    @NotBlank(message = "Provider is required.")
    @Size(max = LlmConstants.PROVIDER_MAX_LENGTH)
    private String provider;

    @NotBlank(message = "Model is required.")
    @Size(max = LlmConstants.MODEL_MAX_LENGTH)
    private String model;

    @Size(max = LlmConstants.API_KEY_REFERENCE_MAX_LENGTH)
    private String apiKeyReference;

    private Double temperature;

    private Integer maxTokens;

    @Size(max = LlmConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}