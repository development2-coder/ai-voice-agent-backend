package com.infinitio.aivoiceplatform.stt.dto.request;

import com.infinitio.aivoiceplatform.stt.constant.SttConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Create STT Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSttRequest {

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "STT code is required.")
    @Size(max = SttConstants.STT_CODE_MAX_LENGTH)
    private String sttCode;

    @NotBlank(message = "STT name is required.")
    @Size(max = SttConstants.STT_NAME_MAX_LENGTH)
    private String sttName;

    @NotBlank(message = "Provider is required.")
    @Size(max = SttConstants.PROVIDER_MAX_LENGTH)
    private String provider;

    @NotBlank(message = "Model is required.")
    @Size(max = SttConstants.MODEL_MAX_LENGTH)
    private String model;

    @NotBlank(message = "Language is required.")
    @Size(max = SttConstants.LANGUAGE_MAX_LENGTH)
    private String language;

    @Size(max = SttConstants.API_KEY_REFERENCE_MAX_LENGTH)
    private String apiKeyReference;

    @Size(max = SttConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}