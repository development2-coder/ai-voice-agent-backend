package com.infinitio.aivoiceplatform.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used to process caller audio.
 *
 * <p>
 * The orchestrator passes the audio to the configured
 * speech-to-text runtime for transcription.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessAudioRequestDto {

    /**
     * Unique public identifier of the call.
     */
    @NotBlank
    private String callId;

    /**
     * Base64 encoded caller audio.
     */
    @NotBlank
    private String audioBase64;

    /**
     * MIME type of the audio.
     *
     * <p>
     * Example: audio/wav or audio/mpeg.
     * </p>
     */
    private String contentType;

    /**
     * Original audio file name, when available.
     */
    private String fileName;

    /**
     * Language supplied for transcription.
     */
    private String language;

    /**
     * Indicates whether the audio represents
     * a final transcription segment.
     */
    @Builder.Default
    private boolean finalTranscript = true;
}