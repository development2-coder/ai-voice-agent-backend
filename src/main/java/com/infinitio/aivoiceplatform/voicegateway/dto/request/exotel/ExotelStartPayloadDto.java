package com.infinitio.aivoiceplatform.voicegateway.dto.request.exotel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the provider-specific START payload received from
 * Exotel when a real-time voice stream is initialized.
 *
 * <p>
 * This DTO belongs to the Exotel integration boundary only.
 * It must be converted into the normalized
 * VoiceGatewayStartRequestDto before entering the application
 * runtime.
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExotelStartPayloadDto {

    /**
     * Provider stream identifier.
     */
    @JsonProperty("stream_sid")
    private String streamSid;

    /**
     * Provider call identifier.
     */
    @JsonProperty("call_sid")
    private String callSid;

    /**
     * Provider account identifier.
     */
    @JsonProperty("account_sid")
    private String accountSid;

    /**
     * Audio format information supplied by Exotel.
     */
    @JsonProperty("media_format")
    private ExotelMediaFormatDto mediaFormat;

    /**
     * Provider-specific start metadata.
     */
    @JsonProperty("custom_parameters")
    private Object customParameters;
}