package com.infinitio.aivoiceplatform.stt.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the synchronous Sarvam STT response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SarvamSttResponse {

    /**
     * Sarvam request identifier.
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * Transcribed text.
     */
    private String transcript;

    /**
     * Detected language code.
     */
    @JsonProperty("language_code")
    private String languageCode;

    /**
     * Language detection probability.
     */
    @JsonProperty("language_probability")
    private Double languageProbability;
}