package com.infinitio.aivoiceplatform.stt.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * STT Configuration Entity.
 *
 * Represents Speech-to-Text configuration
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
        name = "stts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stt_code",
                        columnNames = "stt_code"
                )
        }
)
public class Stt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @Column(
            name = "stt_code",
            nullable = false,
            length = 50
    )
    private String sttCode;

    @Column(
            name = "stt_name",
            nullable = false,
            length = 150
    )
    private String sttName;

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