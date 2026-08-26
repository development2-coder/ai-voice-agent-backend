package com.infinitio.aivoiceplatform.voice.dto.response;

import lombok.*;

/**
 * Voice Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceResponse {

    private String publicId;

    private String agentPublicId;

    private String voiceCode;

    private String voiceName;

    private String provider;

    private String voiceId;

    private String language;

    private String gender;

    private String description;

    private Integer isActive;
}