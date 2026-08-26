package com.infinitio.aivoiceplatform.transcript.entity;

import java.time.LocalDateTime;

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
 * Represents one transcript segment generated during a voice call.
 *
 * <p>
 * A transcript belongs directly to a call. A call recording is not
 * required because transcription can be generated from the live
 * conversation independently of recording availability.
 * </p>
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
        name = "transcripts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transcript_call_sequence",
                        columnNames = {
                                "call_id",
                                "sequence_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transcript_call",
                        columnList = "call_id"
                ),
                @Index(
                        name = "idx_transcript_call_sequence",
                        columnList = "call_id, sequence_number"
                ),
                @Index(
                        name = "idx_transcript_speaker_type",
                        columnList = "speaker_type"
                )
        }
)
public class Transcript extends BaseEntity {

    /**
     * Call to which this transcript segment belongs.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "call_id",
            nullable = false
    )
    private Call call;

    /**
     * Sequence number of this transcript segment within the call.
     */
    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;

    /**
     * Speaker that produced the transcript.
     *
     * <p>
     * Example values:
     * USER, ASSISTANT, SYSTEM.
     * </p>
     */
    @Column(
            name = "speaker_type",
            nullable = false,
            length = 30
    )
    private String speakerType;

    /**
     * Transcribed text.
     */
    @Column(
            name = "text",
            nullable = false,
            length = 5000
    )
    private String text;

    /**
     * Language used for this transcript segment.
     */
    @Column(
            name = "language",
            length = 20
    )
    private String language;

    /**
     * Source of the transcript.
     *
     * <p>
     * Example values:
     * STT, MANUAL, SYSTEM.
     * </p>
     */
    @Column(
            name = "source",
            length = 30
    )
    private String source;

    /**
     * Start time of the transcript segment.
     */
    @Column(
            name = "started_at"
    )
    private LocalDateTime startedAt;

    /**
     * End time of the transcript segment.
     */
    @Column(
            name = "ended_at"
    )
    private LocalDateTime endedAt;
}