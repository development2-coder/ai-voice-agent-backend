package com.infinitio.aivoiceplatform.tts.dto.response;

import lombok.*;

/**
 * TTS Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsResponse {

    private String publicId;

    private String agentPublicId;

    private String ttsCode;

    private String ttsName;

    private String provider;

    private String model;

    private String language;

    private String voiceId;

    private String apiKeyReference;

    private String description;

    private Integer isActive;
}
