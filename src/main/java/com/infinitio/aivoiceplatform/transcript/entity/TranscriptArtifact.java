package com.infinitio.aivoiceplatform.transcript.entity;

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
        name = "transcript_artifacts",
        indexes = {
                @Index(
                        name = "idx_transcript_artifact_call",
                        columnList = "call_public_id"
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptArtifact
        extends BaseEntity {

    @Column(
            name = "call_public_id",
            nullable = false,
            length = 100
    )
    private String callPublicId;

    @Column(
            name = "file_path",
            nullable = false,
            length = 1000
    )
    private String filePath;

    @Column(
            name = "file_name",
            nullable = false,
            length = 255
    )
    private String fileName;

    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;

    @Column(
            name = "size_bytes"
    )
    private Long sizeBytes;
}