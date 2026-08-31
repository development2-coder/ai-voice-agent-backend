package com.infinitio.aivoiceplatform.voicegateway.service.impl;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationOrchestratorService;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayConstants;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayMessages;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayDtmfRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayMediaRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStartRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStopRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.response.VoiceGatewayResponseDto;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayCallContextService;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * Default implementation of the Voice Gateway service.
 *
 * <p>
 * This service acts as the runtime bridge between the
 * telephony WebSocket layer and the Conversation Orchestrator.
 * </p>
 *
 * <p>
 * The Voice Gateway is responsible for transport-level
 * validation and event handling. Tenant-specific runtime
 * behaviour is delegated to the Conversation Orchestrator.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VoiceGatewayServiceImpl
        implements VoiceGatewayService {

    private final VoiceGatewayCallContextService
            callContextService;

    private final ConversationOrchestratorService
            conversationOrchestratorService;

    // =========================================================
    // START STREAM
    // =========================================================

    /**
     * Starts the Voice Gateway stream.
     *
     * @param request Voice Gateway start request
     * @return Voice Gateway response
     */
    @Override
    public VoiceGatewayResponseDto startStream(
            VoiceGatewayStartRequestDto request) {

        validateStartRequest(
                request
        );

        log.info(
                "{} Starting voice stream. " +
                        "callId={}, providerCallId={}, streamId={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                request.getProviderCallId(),
                request.getStreamId()
        );

        CallSessionResponseDto callSession =
                callContextService.resolveCallSession(
                        request.getCallId()
                );

        if (callSession == null) {

            log.error(
                    "{} Call Session could not be resolved. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId()
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }

        String tenantId =
                callSession.getTenantId();

        String agentId =
                callSession.getAgentId();

        Integer agentVersion =
                callSession.getAgentVersion();

        String flowPublicId =
                callSession.getFlowPublicId();

        String language =
                callSession.getLanguage();

        if (tenantId == null
                || tenantId.isBlank()) {

            log.error(
                    "{} Tenant ID is missing from Call Session. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId()
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }

        if (agentId == null
                || agentId.isBlank()) {

            log.error(
                    "{} Agent ID is missing from Call Session. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId()
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }

        if (flowPublicId == null
                || flowPublicId.isBlank()) {

            log.error(
                    "{} Flow public ID is missing from Call Session. " +
                            "callId={}, tenantId={}, agentId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId(),
                    tenantId,
                    agentId
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }

        log.info(
                "{} Runtime context resolved. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "agentVersion={}, flowPublicId={}, language={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                tenantId,
                agentId,
                agentVersion,
                flowPublicId,
                language
        );

        /*
         * The Voice Gateway does not choose the Flow.
         *
         * The Flow was selected when the Call Session was created
         * and is now recovered from that persisted runtime state.
         */
        StartConversationRequestDto conversationRequest =
                StartConversationRequestDto.builder()
                        .callId(
                                request.getCallId()
                        )
                        .tenantId(
                                tenantId
                        )
                        .agentId(
                                agentId
                        )
                        .agentVersion(
                                agentVersion
                        )
                        .flowPublicId(
                                flowPublicId
                        )
                        .language(
                                language
                        )
                        .build();

        ConversationOrchestratorResponseDto
                orchestratorResponse =
                conversationOrchestratorService.start(
                        conversationRequest
                );

        log.info(
                "{} Voice conversation initialized. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "flowPublicId={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                tenantId,
                agentId,
                flowPublicId
        );

        return buildResponse(
                request.getCallId(),
                request.getStreamId(),
                orchestratorResponse
        );
    }

    // =========================================================
    // MEDIA
    // =========================================================

    /**
     * Processes an incoming media packet.
     *
     * <p>
     * The current implementation validates and decodes the
     * incoming audio packet. Streaming STT integration will
     * consume these decoded chunks in the next runtime step.
     * </p>
     *
     * @param request Voice Gateway media request
     * @return Voice Gateway response
     */
    @Override
    public VoiceGatewayResponseDto processMedia(
            VoiceGatewayMediaRequestDto request) {

        validateMediaRequest(
                request
        );

        log.debug(
                "{} Processing incoming media. " +
                        "callId={}, streamId={}, sequenceNumber={}, " +
                        "chunk={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                request.getStreamId(),
                request.getSequenceNumber(),
                request.getChunk()
        );

        byte[] audioBytes =
                decodeAudio(
                        request.getAudioBase64()
                );

        if (audioBytes.length == 0) {

            log.warn(
                    "{} Empty decoded audio received. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId()
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_AUDIO_PAYLOAD
            );
        }

        /*
         * Use only fields that actually exist in
         * VoiceGatewayMediaRequestDto.
         */
        log.debug(
                "{} Incoming audio accepted. " +
                        "callId={}, audioBytes={}, " +
                        "encoding={}, sampleRate={}, channels={}, " +
                        "sampleSizeBits={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                audioBytes.length,
                request.getAudioEncoding(),
                request.getSampleRate(),
                request.getChannels(),
                request.getSampleSizeBits()
        );

        return VoiceGatewayResponseDto.builder()
                .callId(
                        request.getCallId()
                )
                .streamId(
                        request.getStreamId()
                )
                .action(
                        VoiceGatewayConstants.ACTION_LISTEN
                )
                .listen(
                        true
                )
                .build();
    }

    // =========================================================
    // DTMF
    // =========================================================

    /**
     * Processes DTMF input.
     *
     * @param request DTMF request
     * @return Voice Gateway response
     */
    @Override
    public VoiceGatewayResponseDto processDtmf(
            VoiceGatewayDtmfRequestDto request) {

        validateDtmfRequest(
                request
        );

        log.info(
                "{} Processing DTMF. " +
                        "callId={}, streamId={}, digit={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                request.getStreamId(),
                request.getDigit()
        );

        return VoiceGatewayResponseDto.builder()
                .callId(
                        request.getCallId()
                )
                .streamId(
                        request.getStreamId()
                )
                .action(
                        VoiceGatewayConstants.ACTION_LISTEN
                )
                .listen(
                        true
                )
                .build();
    }

    // =========================================================
    // STOP STREAM
    // =========================================================

    /**
     * Stops the Voice Gateway stream.
     *
     * @param request stop request
     * @return Voice Gateway response
     */
    @Override
    public VoiceGatewayResponseDto stopStream(
            VoiceGatewayStopRequestDto request) {

        validateStopRequest(
                request
        );

        log.info(
                "{} Stopping voice stream. " +
                        "callId={}, providerCallId={}, " +
                        "streamId={}, reason={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                request.getProviderCallId(),
                request.getStreamId(),
                request.getReason()
        );

        return VoiceGatewayResponseDto.builder()
                .callId(
                        request.getCallId()
                )
                .streamId(
                        request.getStreamId()
                )
                .action(
                        VoiceGatewayConstants.ACTION_END
                )
                .endCall(
                        true
                )
                .build();
    }

    // =========================================================
    // BARGE-IN
    // =========================================================

    /**
     * Processes caller barge-in.
     *
     * @param callId application Call ID
     * @return Voice Gateway response
     */
    @Override
    public VoiceGatewayResponseDto processBargeIn(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }

        log.info(
                "{} Barge-in received. callId={}",
                VoiceGatewayConstants.LOG_PREFIX,
                callId
        );

        return VoiceGatewayResponseDto.builder()
                .callId(
                        callId
                )
                .action(
                        VoiceGatewayConstants.ACTION_LISTEN
                )
                .listen(
                        true
                )
                .clearAudio(
                        true
                )
                .build();
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    /**
     * Converts the Conversation Orchestrator response into
     * a Voice Gateway response.
     *
     * @param callId application Call ID
     * @param streamId provider stream ID
     * @param response orchestrator response
     * @return Voice Gateway response
     */
    private VoiceGatewayResponseDto buildResponse(
            String callId,
            String streamId,
            ConversationOrchestratorResponseDto response) {

        if (response == null) {

            log.warn(
                    "{} Conversation Orchestrator returned null. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    callId
            );

            return VoiceGatewayResponseDto.builder()
                    .callId(
                            callId
                    )
                    .streamId(
                            streamId
                    )
                    .action(
                            VoiceGatewayConstants.ACTION_LISTEN
                    )
                    .listen(
                            true
                    )
                    .build();
        }

        /*
         * The transport response remains intentionally simple
         * until the TTS audio streaming path is connected.
         */
        return VoiceGatewayResponseDto.builder()
                .callId(
                        callId
                )
                .streamId(
                        streamId
                )
                .action(
                        VoiceGatewayConstants.ACTION_LISTEN
                )
                .listen(
                        true
                )
                .build();
    }

    // =========================================================
    // AUDIO
    // =========================================================

    /**
     * Decodes a Base64 audio payload.
     *
     * @param audioBase64 Base64 audio payload
     * @return decoded audio bytes
     */
    private byte[] decodeAudio(
            String audioBase64) {

        try {

            return Base64.getDecoder()
                    .decode(
                            audioBase64
                    );

        } catch (IllegalArgumentException exception) {

            log.warn(
                    "{} Invalid Base64 audio payload.",
                    VoiceGatewayConstants.LOG_PREFIX
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_AUDIO_PAYLOAD,
                    exception
            );
        }
    }

    // =========================================================
    // RUNTIME VALIDATION
    // =========================================================

    /**
     * Validates the runtime Call Session context.
     *
     * @param callSession Call Session response
     */
    private void validateRuntimeContext(
            CallSessionResponseDto callSession) {

        if (callSession.getTenantId() == null
                || callSession.getTenantId().isBlank()) {

            log.error(
                    "{} Tenant ID missing from Call Session. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    callSession.getCallId()
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }

        if (callSession.getAgentId() == null
                || callSession.getAgentId().isBlank()) {

            log.error(
                    "{} Agent ID missing from Call Session. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    callSession.getCallId()
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }
    }

    // =========================================================
    // REQUEST VALIDATION
    // =========================================================

    private void validateStartRequest(
            VoiceGatewayStartRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_START_EVENT
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }

        if (request.getStreamId() == null
                || request.getStreamId().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.STREAM_ID_REQUIRED
            );
        }
    }

    private void validateMediaRequest(
            VoiceGatewayMediaRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_MEDIA_EVENT
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }

        if (request.getStreamId() == null
                || request.getStreamId().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.STREAM_ID_REQUIRED
            );
        }

        if (request.getAudioBase64() == null
                || request.getAudioBase64().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.AUDIO_PAYLOAD_REQUIRED
            );
        }
    }

    private void validateDtmfRequest(
            VoiceGatewayDtmfRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_DTMF_EVENT
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }

        if (request.getDigit() == null
                || request.getDigit().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.DTMF_DIGIT_REQUIRED
            );
        }
    }

    private void validateStopRequest(
            VoiceGatewayStopRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_STOP_EVENT
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }

        if (request.getStreamId() == null
                || request.getStreamId().isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.STREAM_ID_REQUIRED
            );
        }
    }
}