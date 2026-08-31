package com.infinitio.aivoiceplatform.voicegateway.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO representing a voice stream stop event received
 * by the Voice Gateway.
 *
 * <p>
 * The stop event indicates that the telephony provider has
 * terminated the media stream. The Voice Gateway uses this
 * event to stop media processing, finalize recording and
 * terminate the associated conversation runtime.
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
public class VoiceGatewayStopRequestDto {

    /**
     * Internal call identifier.
     */
    private String callId;

    /**
     * Provider call identifier.
     */
    private String providerCallId;

    /**
     * Provider stream identifier.
     */
    private String streamId;

    /**
     * Sequence number of the provider event.
     */
    private Long sequenceNumber;

    /**
     * Provider supplied stop reason.
     */
    private String reason;

    /**
     * Timestamp supplied by the provider.
     */
    private Long timestamp;

    /**
     * Indicates whether the provider has already terminated
     * the call.
     */
    private boolean callTerminated;
}