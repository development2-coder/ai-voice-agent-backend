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
 * <p>
 * This DTO contains provider-neutral outbound call
 * capabilities. Provider-specific mapping is handled
 * by the selected {@code TelephonyProvider} implementation.
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
public class PlaceOutboundCallRequestDto {

    /**
     * Application call public ID used for call correlation.
     */
    @NotBlank
    private String callPublicId;

    /**
     * Number from which the outbound call is initiated.
     */
    @NotBlank
    private String fromNumber;

    /**
     * Destination number.
     */
    @NotBlank
    private String toNumber;

    /**
     * Optional status callback URL.
     *
     * <p>
     * When not supplied, the selected telephony provider
     * may use its configured callback URL.
     * </p>
     */
    private String callbackUrl;

    /**
     * Application-owned real-time media streaming URL.
     */
    private String streamUrl;

    /**
     * Requested media streaming type.
     */
    private String streamType;

    /**
     * Whether call recording is requested.
     */
    private Boolean record;

    /**
     * Requested recording channel mode.
     */
    private String recordingChannels;

    /**
     * Optional maximum call duration in seconds.
     */
    private Integer timeLimit;
}