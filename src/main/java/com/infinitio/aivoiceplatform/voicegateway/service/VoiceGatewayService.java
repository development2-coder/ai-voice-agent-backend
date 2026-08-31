package com.infinitio.aivoiceplatform.voicegateway.service;

import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayDtmfRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayMediaRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStartRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStopRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.response.VoiceGatewayResponseDto;

/**
 * Defines the provider-neutral Voice Gateway runtime contract.
 *
 * <p>
 * This service coordinates real-time voice events received from
 * a telephony provider and forwards them to the appropriate
 * application runtime components.
 * </p>
 *
 * <p>
 * The WebSocket layer should only be responsible for receiving
 * and sending provider messages. Business and runtime processing
 * must be handled through this service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface VoiceGatewayService {

    /**
     * Initializes a new voice stream.
     *
     * <p>
     * The start event associates the provider stream with the
     * application's Call, Tenant, Agent and Flow configuration.
     * </p>
     *
     * @param request stream start request
     * @return initial gateway response
     */
    VoiceGatewayResponseDto startStream(
            VoiceGatewayStartRequestDto request
    );

    /**
     * Processes an incoming caller audio packet.
     *
     * <p>
     * The audio is forwarded to the streaming voice runtime.
     * The same incoming media must also be available to the
     * call-recording pipeline so that the complete call can
     * eventually be stored as an audio file.
     * </p>
     *
     * @param request media request
     * @return gateway response
     */
    VoiceGatewayResponseDto processMedia(
            VoiceGatewayMediaRequestDto request
    );

    /**
     * Processes a caller DTMF event.
     *
     * <p>
     * DTMF is passed to the Conversation Orchestrator and
     * ultimately handled according to the tenant's Flow.
     * </p>
     *
     * @param request DTMF request
     * @return gateway response
     */
    VoiceGatewayResponseDto processDtmf(
            VoiceGatewayDtmfRequestDto request
    );

    /**
     * Processes a provider stream stop event.
     *
     * <p>
     * Stops runtime processing, finalizes recording and
     * terminates the associated conversation.
     * </p>
     *
     * @param request stream stop request
     * @return gateway response
     */
    VoiceGatewayResponseDto stopStream(
            VoiceGatewayStopRequestDto request
    );

    /**
     * Processes caller barge-in.
     *
     * <p>
     * A barge-in indicates that the caller started speaking while
     * AI audio was being played. The Voice Gateway must stop or
     * clear buffered AI audio and allow caller input to continue.
     *
     * @param callId internal call identifier
     * @return gateway response
     */
    VoiceGatewayResponseDto processBargeIn(
            String callId
    );
}