package com.infinitio.aivoiceplatform.voice.dto.request;

import com.infinitio.aivoiceplatform.voice.constant.VoiceConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Voice Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVoiceRequest {

    @NotBlank(message = "Public Id is required.")
    private String publicId;

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "Voice code is required.")
    @Size(max = VoiceConstants.VOICE_CODE_MAX_LENGTH)
    private String voiceCode;

    @NotBlank(message = "Voice name is required.")
    @Size(max = VoiceConstants.VOICE_NAME_MAX_LENGTH)
    private String voiceName;

    @NotBlank(message = "Provider is required.")
    @Size(max = VoiceConstants.PROVIDER_MAX_LENGTH)
    private String provider;

    @NotBlank(message = "Voice ID is required.")
    @Size(max = VoiceConstants.VOICE_ID_MAX_LENGTH)
    private String voiceId;

    @NotBlank(message = "Language is required.")
    @Size(max = VoiceConstants.LANGUAGE_MAX_LENGTH)
    private String language;

    @Size(max = VoiceConstants.GENDER_MAX_LENGTH)
    private String gender;

    @Size(max = VoiceConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}