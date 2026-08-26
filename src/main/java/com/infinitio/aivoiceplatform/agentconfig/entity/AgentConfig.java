package com.infinitio.aivoiceplatform.agentconfig.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigConstants;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Agent Configuration Entity.
 *
 * Stores the AI configuration used by an Agent.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "agent_configs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_agent_config_agent",
                        columnNames = "agent_id"
                )
        }
)
public class AgentConfig extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;


    @Column(
            name = "llm_provider",
            nullable = false,
            length = AgentConfigConstants.PROVIDER_MAX_LENGTH
    )
    private String llmProvider;


    @Column(
            name = "llm_model",
            nullable = false,
            length = AgentConfigConstants.MODEL_MAX_LENGTH
    )
    private String llmModel;


    @Column(
            name = "stt_provider",
            nullable = false,
            length = AgentConfigConstants.PROVIDER_MAX_LENGTH
    )
    private String sttProvider;


    @Column(
            name = "stt_model",
            nullable = false,
            length = AgentConfigConstants.MODEL_MAX_LENGTH
    )
    private String sttModel;


    @Column(
            name = "tts_provider",
            nullable = false,
            length = AgentConfigConstants.PROVIDER_MAX_LENGTH
    )
    private String ttsProvider;


    @Column(
            name = "tts_model",
            nullable = false,
            length = AgentConfigConstants.MODEL_MAX_LENGTH
    )
    private String ttsModel;


    @Column(
            name = "language",
            nullable = false,
            length = AgentConfigConstants.LANGUAGE_MAX_LENGTH
    )
    private String language;


    @Column(
            name = "voice",
            length = AgentConfigConstants.VOICE_MAX_LENGTH
    )
    private String voice;


    @Column(
            name = "greeting_message",
            length = AgentConfigConstants.GREETING_MAX_LENGTH
    )
    private String greetingMessage;


    @Column(
            name = "system_prompt",
            columnDefinition = "TEXT"
    )
    private String systemPrompt;


    @Column(
            name = "temperature",
            precision = 4,
            scale = 2
    )
    private BigDecimal temperature;


    @Column(name = "max_tokens")
    private Integer maxTokens;


    @Column(
            name = "status",
            nullable = false,
            length = AgentConfigConstants.STATUS_MAX_LENGTH
    )
    private String status;


    @PrePersist
    public void initializeDefaults() {

        if (status == null
                || status.isBlank()) {

            status =
                    AgentConfigConstants.STATUS_DRAFT;
        }

        if (temperature == null) {

            temperature =
                    BigDecimal.valueOf(
                            AgentConfigConstants.DEFAULT_TEMPERATURE
                    );
        }

        if (maxTokens == null) {

            maxTokens =
                    AgentConfigConstants.DEFAULT_MAX_TOKENS;
        }
    }
}