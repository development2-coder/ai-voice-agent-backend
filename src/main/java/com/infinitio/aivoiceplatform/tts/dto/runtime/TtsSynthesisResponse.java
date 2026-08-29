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
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsSynthesisResponse {

    private String callId;

    private String audioBase64;

    private String audioUrl;

    private String fileName;

    /**
     * Absolute/local filesystem path of the generated audio.
     *
     * <p>
     * This value is persisted in tts_interactions.
     * </p>
     */
    private String filePath;

    private String contentType;

    private String language;

    private String speaker;

    private String provider;

    private String model;

    private boolean finalResponse;

    private Long latencyMs;

    private Integer inputCharacters;

    private String providerRequestId;

    /**
     * Internal raw audio bytes.
     *
     * Never serialize this field in the API response.
     */
    @JsonIgnore
    private byte[] audioBytes;
}