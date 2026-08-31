package com.infinitio.aivoiceplatform.voicegateway.dto.request.exotel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents audio-format information supplied by Exotel
 * during stream initialization.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExotelMediaFormatDto {

    /**
     * Audio encoding.
     */
    @JsonProperty("encoding")
    private String encoding;

    /**
     * Audio sample rate.
     */
    @JsonProperty("sample_rate")
    private Integer sampleRate;

    /**
     * Number of audio channels.
     */
    @JsonProperty("channels")
    private Integer channels;
}