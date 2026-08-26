package com.infinitio.aivoiceplatform.tts.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * TTS Configuration Entity.
 *
 * Represents Text-to-Speech configuration
 * for an AI Voice Agent.
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
        name = "tts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tts_code",
                        columnNames = "tts_code"
                )
        }
)
public class Tts extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @Column(
            name = "tts_code",
            nullable = false,
            length = 50
    )
    private String ttsCode;

    @Column(
            name = "tts_name",
            nullable = false,
            length = 150
    )
    private String ttsName;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "model",
            nullable = false,
            length = 150
    )
    private String model;

    @Column(
            name = "language",
            nullable = false,
            length = 50
    )
    private String language;

    @Column(
            name = "voice_id",
            nullable = false,
            length = 150
    )
    private String voiceId;

    @Column(
            name = "api_key_reference",
            length = 500
    )
    private String apiKeyReference;

    @Column(
            name = "description",
            length = 500
    )
    private String description;
}