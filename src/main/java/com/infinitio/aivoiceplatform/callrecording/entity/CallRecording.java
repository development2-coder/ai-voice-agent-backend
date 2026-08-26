package com.infinitio.aivoiceplatform.callrecording.entity;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Call Recording Entity.
 *
 * Represents an audio/video recording associated
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
        }
)
public class CallRecording extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(
            name = "file_url",
            nullable = false,
            length = 500
    )
    private String fileUrl;

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

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(
            name = "description",
            length = 500
    )
    private String description;
}