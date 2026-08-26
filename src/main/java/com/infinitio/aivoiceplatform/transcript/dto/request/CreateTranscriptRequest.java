package com.infinitio.aivoiceplatform.transcript.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a transcript segment.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTranscriptRequest {

    /**
     * Public identifier of the call.
     */
    @NotBlank
    private String callPublicId;

    /**
     * Sequence number of the transcript segment.
     */
    @NotNull
    @Min(1)
    private Integer sequenceNumber;

    /**
     * Speaker type.
     *
     * <p>
     * Example values:
     * USER, ASSISTANT, SYSTEM.
     * </p>
     */
    @NotBlank
    @Size(max = 30)
    private String speakerType;

    /**
     * Transcript text.
     */
    @NotBlank
    @Size(max = 5000)
    private String text;

    /**
     * Language of the transcript.
     */
    @Size(max = 20)
    private String language;

    /**
     * Transcript source.
     *
     * <p>
     * Example values:
     * STT, MANUAL, SYSTEM.
     * </p>
     */
    @Size(max = 30)
    private String source;

    /**
     * Transcript start time.
     */
    private LocalDateTime startedAt;

    /**
     * Transcript end time.
     */
    private LocalDateTime endedAt;
}