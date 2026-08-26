package com.infinitio.aivoiceplatform.llm.dto.response;

import lombok.*;

/**
 * LLM Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {

    private String publicId;

    private String agentPublicId;

    private String llmCode;

    private String llmName;

    private String provider;

    private String model;

    private String apiKeyReference;

    private Double temperature;

    private Integer maxTokens;

    private String description;

    private Integer isActive;
}