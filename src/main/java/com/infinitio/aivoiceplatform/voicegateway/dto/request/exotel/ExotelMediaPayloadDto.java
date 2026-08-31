package com.infinitio.aivoiceplatform.voicegateway.dto.request.exotel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the media payload received from Exotel's
 * real-time bidirectional voice stream.
 *
 * <p>
 * This DTO is intentionally limited to provider-specific
 * transport data. It is converted into the normalized
 * VoiceGatewayMediaRequestDto before entering the application
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
public class ExotelMediaPayloadDto {

    /**
     * Provider media chunk number.
     */
    @JsonProperty("chunk")
    private Long chunk;

    /**
     * Provider timestamp associated with the media packet.
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * Base64 encoded audio payload.
     */
    @JsonProperty("payload")
    private String payload;
}