package com.infinitio.aivoiceplatform.voicegateway.dto.request.exotel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the provider-specific DTMF payload received from
 * Exotel during a real-time voice stream.
 *
 * <p>
 * This DTO is used only at the Exotel integration boundary.
 * It must be converted into the normalized
 * VoiceGatewayDtmfRequestDto before being passed to the
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
public class ExotelDtmfPayloadDto {

    /**
     * DTMF digit received from the caller.
     */
    @JsonProperty("digit")
    private String digit;

    /**
     * Provider stream identifier, when supplied.
     */
    @JsonProperty("stream_sid")
    private String streamSid;

    /**
     * Provider sequence number, when supplied.
     */
    @JsonProperty("sequence_number")
    private Long sequenceNumber;

    /**
     * Provider timestamp, when supplied.
     */
    @JsonProperty("timestamp")
    private Long timestamp;
}