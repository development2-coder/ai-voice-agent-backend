package com.infinitio.aivoiceplatform.voicegateway.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the common incoming WebSocket event received
 * by the Voice Gateway from a telephony provider.
 *
 * <p>
 * This DTO acts as the provider-event envelope. The Voice Gateway
 * first reads the event type and then converts the provider payload
 * into the appropriate normalized request DTO.
 * </p>
 *
 * <p>
 * Provider-specific fields are intentionally kept minimal here.
 * Detailed START, MEDIA, DTMF and STOP information belongs to
 * their respective request DTOs.
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
public class VoiceGatewayEventRequestDto {

    /**
     * Event type received from the telephony provider.
     *
     * <p>
     * Examples:
     * connected, start, media, dtmf, mark and stop.
     * </p>
     */
    @NotBlank
    private String event;

    /**
     * Provider stream identifier.
     */
    private String streamId;

    /**
     * Provider sequence number.
     */
    private Long sequenceNumber;

    /**
     * Provider call identifier.
     */
    private String providerCallId;

    /**
     * Raw provider event payload.
     *
     * <p>
     * This is retained only at the gateway boundary and should
     * not be passed directly into business services.
     * </p>
     */
    private String payload;
}