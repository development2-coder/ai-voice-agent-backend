package com.infinitio.aivoiceplatform.tts.dto.request;

import com.infinitio.aivoiceplatform.tts.constant.TtsConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update TTS Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTtsRequest {

    @NotBlank(message = "Public Id is required.")
    private String publicId;

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "TTS code is required.")
    @Size(max = TtsConstants.TTS_CODE_MAX_LENGTH)
    private String ttsCode;

    @NotBlank(message = "TTS name is required.")
    @Size(max = TtsConstants.TTS_NAME_MAX_LENGTH)
    private String ttsName;

    @NotBlank(message = "Provider is required.")
    @Size(max = TtsConstants.PROVIDER_MAX_LENGTH)
    private String provider;

    @NotBlank(message = "Model is required.")
    @Size(max = TtsConstants.MODEL_MAX_LENGTH)
    private String model;

    @NotBlank(message = "Language is required.")
    @Size(max = TtsConstants.LANGUAGE_MAX_LENGTH)
    private String language;

    @NotBlank(message = "Voice ID is required.")
    @Size(max = TtsConstants.VOICE_ID_MAX_LENGTH)
    private String voiceId;

    @Size(max = TtsConstants.API_KEY_REFERENCE_MAX_LENGTH)
    private String apiKeyReference;

    @Size(max = TtsConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}