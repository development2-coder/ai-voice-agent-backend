package com.infinitio.aivoiceplatform.voicegateway.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO representing a normalized incoming media event
 * received by the Voice Gateway.
 *
 * <p>
 * Each media request represents one audio chunk received from
 * the caller through the telephony provider's real-time
 * WebSocket stream.
 * </p>
 *
 * <p>
 * The same decoded audio is intended to be consumed by both:
 * </p>
 *
 * <ul>
 *     <li>Streaming STT for real-time transcription</li>
 *     <li>Call recording storage for the complete conversation</li>
 * </ul>
 *
 * <p>
 * The Voice Gateway does not decide which Flow node should
 * execute next. The Conversation Orchestrator and Flow Engine
 * make that decision using the tenant's configured Agent Flow.
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
public class VoiceGatewayMediaRequestDto {

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
     * Provider sequence number.
     */
    private Long sequenceNumber;

    /**
     * Provider media chunk number.
     */
    private Long chunk;

    /**
     * Provider timestamp associated with the media packet.
     */
    private Long timestamp;

    /**
     * Base64 encoded audio payload.
     *
     * <p>
     * The gateway decodes this value before passing the raw
     * audio bytes to the streaming runtime and recording
     * pipeline.
     * </p>
     */
    @NotBlank
    private String audioBase64;

    /**
     * Audio encoding used by the provider.
     *
     * <p>
     * For the current Exotel streaming configuration this is
     * expected to represent linear PCM/slin audio.
     * </p>
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
     * Number of bits per audio sample.
     */
    private Integer sampleSizeBits;

    /**
     * Indicates whether this media packet belongs to the
     * caller/inbound audio direction.
     */
    @Builder.Default
    private boolean inbound = true;

    /**
     * Indicates whether the media packet has passed basic
     * gateway validation.
     */
    @Builder.Default
    private boolean valid = true;
}