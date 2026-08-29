package com.infinitio.aivoiceplatform.stt.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "stt_interactions",
        indexes = {
                @Index(
                        name = "idx_stt_interaction_call",
                        columnList = "call_public_id"
                ),
                @Index(
                        name = "idx_stt_interaction_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SttInteraction
        extends BaseEntity {

    @Column(
            name = "call_public_id",
            nullable = false,
            length = 100
    )
    private String callPublicId;

    @Column(
            name = "transcript",
            columnDefinition = "LONGTEXT"
    )
    private String transcript;

    @Column(
            name = "language",
            length = 20
    )
    private String language;

    @Column(
            name = "provider",
            length = 50
    )
    private String provider;

    @Column(
            name = "model",
            length = 100
    )
    private String model;

    @Column(
            name = "final_transcript",
            nullable = false
    )
    private Boolean finalTranscript;

    @Column(
            name = "language_probability"
    )
    private Double languageProbability;

    @Column(
            name = "latency_ms"
    )
    private Long latencyMs;

    @Column(
            name = "audio_size_bytes"
    )
    private Long audioSizeBytes;

    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;

    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    private String errorMessage;
}