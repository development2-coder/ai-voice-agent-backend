package com.infinitio.aivoiceplatform.tts.dto.runtime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the response returned by the runtime text-to-speech operation.
 *
 * <p>
 * The generated audio is returned as Base64 together with metadata.
 * The actual generated audio bytes are retained internally so that the
 * runtime service can store the generated audio file on the backend.
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
public class TtsSynthesisResponse {

    /**
     * Unique call identifier.
     */
    private String callId;

    /**
     * Generated audio encoded as Base64.
     */
    private String audioBase64;

    /**
     * Generated audio file URL.
     */
    private String audioUrl;

    /**
     * Generated audio file name.
     */
    private String fileName;

    /**
     * Audio content type.
     *
     * <p>
     * Example: audio/wav or audio/mpeg.
     * </p>
     */
    private String contentType;

    /**
     * Target language used for synthesis.
     */
    private String language;

    /**
     * Speaker used for synthesis.
     */
    private String speaker;

    /**
     * TTS provider used for synthesis.
     */
    private String provider;

    /**
     * TTS model used for synthesis.
     */
    private String model;

    /**
     * Indicates whether this is the final response for the current
     * conversation turn.
     */
    private boolean finalResponse;

    /**
     * Provider synthesis latency in milliseconds.
     */
    private Long latencyMs;

    /**
     * Number of input characters sent for synthesis.
     */
    private Integer inputCharacters;

    /**
     * Provider request identifier.
     */
    private String providerRequestId;

    /**
     * Raw generated audio bytes.
     *
     * <p>
     * This field is used internally by the backend for file storage.
     * It is intentionally excluded from JSON serialization.
     * </p>
     */
    @JsonIgnore
    private byte[] audioBytes;
}