package com.infinitio.aivoiceplatform.voicegateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO produced by the Voice Gateway runtime.
 *
 * <p>
 * Represents the normalized action that the Voice Gateway
 * should perform after processing an incoming telephony event.
 * </p>
 *
 * <p>
 * The DTO is provider-neutral. Provider-specific WebSocket
 * response formats are created by the provider adapter.
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
public class VoiceGatewayResponseDto {

    /**
     * Internal call identifier.
     */
    private String callId;

    /**
     * Provider stream identifier.
     */
    private String streamId;

    /**
     * Runtime action to perform.
     *
     * <p>
     * Examples:
     * SPEAK, LISTEN, END, TRANSFER.
     * </p>
     */
    private String action;

    /**
     * Base64 encoded audio to send to the telephony provider.
     */
    private String audioBase64;

    /**
     * Audio encoding.
     */
    private String audioEncoding;

    /**
     * Audio sample rate.
     */
    private Integer sampleRate;

    /**
     * Number of audio channels.
     */
    private Integer channels;

    /**
     * Audio content type.
     */
    private String contentType;

    /**
     * Text associated with the response.
     *
     * <p>
     * This can contain the AI response before or alongside
     * synthesized audio.
     * </p>
     */
    private String responseText;

    /**
     * Indicates whether the caller should continue speaking.
     */
    private boolean listen;

    /**
     * Indicates whether the current call should be terminated.
     */
    private boolean endCall;

    /**
     * Indicates whether the call should be transferred.
     */
    private boolean transfer;

    /**
     * Transfer destination when transfer is requested.
     */
    private String transferTo;

    /**
     * Mark name associated with the outbound audio.
     */
    private String markName;

    /**
     * Indicates that the response should clear currently
     * buffered provider audio.
     *
     * <p>
     * This is primarily used during barge-in handling.
     * </p>
     */
    private boolean clearAudio;
}