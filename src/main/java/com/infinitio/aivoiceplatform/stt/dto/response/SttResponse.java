package com.infinitio.aivoiceplatform.stt.dto.response;

import lombok.*;

/**
 * STT Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SttResponse {

    private String publicId;

    private String agentPublicId;

    private String sttCode;

    private String sttName;

    private String provider;

    private String model;

    private String language;

    private String apiKeyReference;

    private String description;

    private Integer isActive;
}