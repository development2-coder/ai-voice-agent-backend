package com.infinitio.aivoiceplatform.tts.entity;

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
        name = "tts_interactions",
        indexes = {
                @Index(
                        name = "idx_tts_interaction_call",
                        columnList = "call_public_id"
                ),
                @Index(
                        name = "idx_tts_interaction_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TtsInteraction
        extends BaseEntity {

    @Column(
            name = "call_public_id",
            nullable = false,
            length = 100
    )
    private String callPublicId;

    @Column(
            name = "text",
            columnDefinition = "LONGTEXT"
    )
    private String text;

    @Column(
            name = "language",
            length = 20
    )
    private String language;

    @Column(
            name = "speaker",
            length = 100
    )
    private String speaker;

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
            name = "final_response",
            nullable = false
    )
    private Boolean finalResponse;

    @Column(
            name = "latency_ms"
    )
    private Long latencyMs;

    @Column(
            name = "input_characters"
    )
    private Integer inputCharacters;

    @Column(
            name = "file_name",
            length = 255
    )
    private String fileName;

    @Column(
            name = "file_path",
            length = 1000
    )
    private String filePath;

    @Column(
            name = "audio_url",
            length = 1000
    )
    private String audioUrl;

    @Column(
            name = "content_type",
            length = 100
    )
    private String contentType;

    @Column(
            name = "audio_size_bytes"
    )
    private Long audioSizeBytes;

    @Column(
            name = "provider_request_id",
            length = 200
    )
    private String providerRequestId;

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