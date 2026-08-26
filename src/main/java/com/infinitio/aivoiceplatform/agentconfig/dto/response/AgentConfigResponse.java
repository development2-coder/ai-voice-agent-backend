package com.infinitio.aivoiceplatform.agentconfig.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * Agent Configuration Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigResponse {

    private String publicId;

    private String agentPublicId;

    private String llmProvider;

    private String llmModel;

    private String sttProvider;

    private String sttModel;

    private String ttsProvider;

    private String ttsModel;

    private String language;

    private String voice;

    private String greetingMessage;

    private String systemPrompt;

    private BigDecimal temperature;

    private Integer maxTokens;

    private String status;

    private Integer isActive;
}