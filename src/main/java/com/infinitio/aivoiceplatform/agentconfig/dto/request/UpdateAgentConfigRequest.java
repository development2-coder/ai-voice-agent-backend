package com.infinitio.aivoiceplatform.agentconfig.dto.request;

import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigConstants;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Update Agent Configuration Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentConfigRequest {

    @NotBlank(message = "Public Id is required.")
    private String publicId;

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "LLM provider is required.")
    @Size(max = AgentConfigConstants.PROVIDER_MAX_LENGTH)
    private String llmProvider;

    @NotBlank(message = "LLM model is required.")
    @Size(max = AgentConfigConstants.MODEL_MAX_LENGTH)
    private String llmModel;

    @NotBlank(message = "STT provider is required.")
    @Size(max = AgentConfigConstants.PROVIDER_MAX_LENGTH)
    private String sttProvider;

    @NotBlank(message = "STT model is required.")
    @Size(max = AgentConfigConstants.MODEL_MAX_LENGTH)
    private String sttModel;

    @NotBlank(message = "TTS provider is required.")
    @Size(max = AgentConfigConstants.PROVIDER_MAX_LENGTH)
    private String ttsProvider;

    @NotBlank(message = "TTS model is required.")
    @Size(max = AgentConfigConstants.MODEL_MAX_LENGTH)
    private String ttsModel;

    @NotBlank(message = "Language is required.")
    @Size(max = AgentConfigConstants.LANGUAGE_MAX_LENGTH)
    private String language;

    @Size(max = AgentConfigConstants.VOICE_MAX_LENGTH)
    private String voice;

    @Size(max = AgentConfigConstants.GREETING_MAX_LENGTH)
    private String greetingMessage;

    @Size(max = AgentConfigConstants.PROMPT_MAX_LENGTH)
    private String systemPrompt;

    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private BigDecimal temperature;

    @Min(1)
    private Integer maxTokens;
}