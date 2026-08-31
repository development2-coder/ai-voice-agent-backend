package com.infinitio.aivoiceplatform.voicegateway.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO representing the normalized stream-start event
 * received by the Voice Gateway.
 *
 * <p>
 * The start event initializes the real-time voice session and
 * provides the information required to associate the provider
 * stream with the application's existing call.
 * </p>
 *
 * <p>
 * Tenant, Agent and Flow configuration must be resolved from
 * the application's call/session data. They should not be trusted
 * from arbitrary provider payload fields.
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
public class VoiceGatewayStartRequestDto {

    /**
     * Internal application call identifier.
     */
    @NotBlank
    private String callId;

    /**
     * Provider-specific call identifier.
     */
    private String providerCallId;

    /**
     * Provider WebSocket stream identifier.
     */
    @NotBlank
    private String streamId;

    /**
     * Provider account identifier.
     */
    private String providerAccountId;

    /**
     * Audio encoding provided by the telephony provider.
     */
    private String audioEncoding;

    /**
     * Audio sample rate in Hertz.
     */
    private Integer sampleRate;

    /**
     * Number of audio channels.
     */
    private Integer channels;

    /**
     * Number of bits per PCM sample.
     */
    private Integer sampleSizeBits;

    /**
     * Caller phone number supplied by the provider.
     */
    private String callerNumber;

    /**
     * Called phone number supplied by the provider.
     */
    private String calledNumber;

    /**
     * Provider-specific metadata.
     */
    private Object metadata;
}