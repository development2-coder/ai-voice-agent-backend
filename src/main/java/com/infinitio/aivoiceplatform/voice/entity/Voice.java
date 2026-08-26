package com.infinitio.aivoiceplatform.voice.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Voice Entity.
 *
 * Represents voice configuration for an AI Voice Agent.
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
        name = "voices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_voice_code",
                        columnNames = "voice_code"
                )
        }
)
public class Voice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @Column(
            name = "voice_code",
            nullable = false,
            length = 50
    )
    private String voiceCode;

    @Column(
            name = "voice_name",
            nullable = false,
            length = 150
    )
    private String voiceName;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "voice_id",
            nullable = false,
            length = 150
    )
    private String voiceId;

    @Column(
            name = "language",
            nullable = false,
            length = 50
    )
    private String language;

    @Column(
            name = "gender",
            length = 30
    )
    private String gender;

    @Column(
            name = "description",
            length = 500
    )
    private String description;
}