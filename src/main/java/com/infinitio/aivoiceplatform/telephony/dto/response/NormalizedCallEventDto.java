package com.infinitio.aivoiceplatform.telephony.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a normalized telephony call event.
 *
 * <p>
 * Provider-specific webhook payloads are converted into this
 * common representation before being passed to downstream services.
 * </p>
 *
 * <p>
 * The DTO also carries optional recording information supplied
 * by the telephony provider when the call recording becomes
 * available.
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
public class NormalizedCallEventDto {

    /**
     * Platform normalized event.
     */
    private String event;

    /**
     * Telephony provider code.
     */
    private String provider;

    /**
     * Provider webhook event identifier.
     */
    private String providerEventId;

    /**
     * Provider call identifier.
     */
    private String providerCallId;

    /**
     * Caller number.
     */
    private String fromNumber;

    /**
     * Destination number.
     */
    private String toNumber;

    /**
     * Provider event timestamp.
     */
    private Instant timestamp;

    /**
     * Recording URL supplied by provider.
     *
     * <p>
     * This value may be null for events such as initiated,
     * ringing and answered. It is normally expected when
     * the provider sends the completed/recording callback.
     * </p>
     */
    private String recordingUrl;

    /**
     * Optional recording duration in seconds.
     */
    private Integer recordingDurationSeconds;

    /**
     * Raw provider webhook payload.
     *
     * <p>
     * This is retained for troubleshooting and auditing.
     * </p>
     */
    private String payload;
}