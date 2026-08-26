package com.infinitio.aivoiceplatform.transcript.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for a transcript segment.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptResponse {

    /**
     * Public identifier of the transcript.
     */
    private String publicId;

    /**
     * Public identifier of the call.
     */
    private String callPublicId;

    /**
     * Sequence number within the call.
     */
    private Integer sequenceNumber;

    /**
     * Speaker type.
     */
    private String speakerType;

    /**
     * Transcript text.
     */
    private String text;

    /**
     * Language of the transcript.
     */
    private String language;

    /**
     * Transcript source.
     */
    private String source;

    /**
     * Start time of the transcript segment.
     */
    private LocalDateTime startedAt;

    /**
     * End time of the transcript segment.
     */
    private LocalDateTime endedAt;

    /**
     * Active status.
     */
    private Integer isActive;

    /**
     * Deleted status.
     */
    private Integer isDeleted;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;
}