package com.infinitio.aivoiceplatform.stt.dto.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an incoming runtime speech-to-text request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SttTranscriptionRequest {

    /**
     * Unique identifier of the active call.
     */
    private String callId;

    /**
     * Audio data to be transcribed.
     */
    private byte[] audio;

    /**
     * MIME type of the audio.
     *
     * <p>
     * Example: audio/wav, audio/mpeg, audio/ogg.
     * </p>
     */
    private String contentType;

    /**
     * Original audio file name.
     */
    private String fileName;

    /**
     * Language requested for transcription.
     *
     * <p>
     * Example: en-IN, hi-IN, mr-IN.
     * </p>
     */
    private String language;

    /**
     * Indicates whether the audio represents a final transcription chunk.
     */
    private boolean finalTranscript;
}