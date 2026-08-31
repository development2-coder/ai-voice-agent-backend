package com.infinitio.aivoiceplatform.voicegateway.dto.request.exotel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the provider-specific STOP payload received from
 * Exotel when a real-time voice stream is terminated.
 *
 * <p>
 * This DTO belongs only to the Exotel integration boundary.
 * It must be converted into the normalized
 * VoiceGatewayStopRequestDto before being passed to the
 * Voice Gateway runtime.
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
public class ExotelStopPayloadDto {

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
     * Reason supplied by the provider for stopping
     * the stream.
     */
    @JsonProperty("reason")
    private String reason;

    /**
     * Provider supplied timestamp.
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * Provider supplied call duration, when available.
     */
    @JsonProperty("duration")
    private Long duration;
}