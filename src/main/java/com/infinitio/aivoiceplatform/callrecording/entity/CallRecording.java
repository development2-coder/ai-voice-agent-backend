package com.infinitio.aivoiceplatform.callrecording.entity;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Call Recording Entity.
 *
 * Represents an audio recording associated
 * with an AI voice call.
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
        name = "call_recordings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_call_recording_url",
                        columnNames = "file_url"
                )
        },
        indexes = {
                @Index(
                        name = "idx_call_recording_call",
                        columnList = "call_id"
                )
        }
)
public class CallRecording
        extends BaseEntity {

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "call_id",
            nullable = false
    )
    private Call call;

    @Column(
            name = "file_name",
            nullable = false,
            length = 255
    )
    private String fileName;

    /**
     * Original provider recording URL.
     */
    @Column(
            name = "file_url",
            nullable = false,
            length = 500
    )
    private String fileUrl;

    /**
     * Local filesystem path.
     */
    @Column(
            name = "file_path",
            length = 1000
    )
    private String filePath;

    @Column(
            name = "file_type",
            nullable = false,
            length = 30
    )
    private String fileType;

    @Column(
            name = "storage_provider",
            length = 50
    )
    private String storageProvider;

    @Column(
            name = "duration_seconds"
    )
    private Integer durationSeconds;

    @Column(
            name = "description",
            length = 500
    )
    private String description;
}