package com.infinitio.aivoiceplatform.telephony.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for placing an outbound call through
 * a telephony provider.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOutboundCallRequestDto {

    @NotBlank
    private String callPublicId;

    @NotBlank
    private String fromNumber;

    @NotBlank
    private String toNumber;

    /**
     * Provider status callback URL.
     */
    private String callbackUrl;

    /**
     * Bidirectional WebSocket URL.
     *
     * Example:
     *
     * wss://abc.ngrok-free.app/ws/telephony/exotel/media
     */
    private String streamUrl;

    /**
     * Stream type.
     *
     * Expected value:
     *
     * bidirectional
     */
    private String streamType;

    /**
     * Whether Exotel should record the call.
     */
    private Boolean record;

    /**
     * Recording channel mode.
     *
     * single / dual
     */
    private String recordingChannels;

    /**
     * Optional maximum call duration.
     */
    private Integer timeLimit;
}