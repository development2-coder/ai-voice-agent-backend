package com.infinitio.aivoiceplatform.voicegateway.service.impl;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationOrchestratorService;
import com.infinitio.aivoiceplatform.stt.provider.SttStreamingListener;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;
import com.infinitio.aivoiceplatform.tts.streaming.TtsAudioStreamRegistry;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayConstants;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayMessages;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayDtmfRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayMediaRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStartRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStopRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.response.VoiceGatewayResponseDto;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayCallContextService;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayService;
import com.infinitio.aivoiceplatform.voicegateway.websocket.VoiceGatewayWebSocketSessionRegistry;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infinitio.aivoiceplatform.runtimepersistence.RuntimePersistenceService;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the Voice Gateway service.
 *
 * <p>
 * The Voice Gateway provides the transport bridge between the
 * provider-specific telephony WebSocket layer and the
 * Conversation Orchestrator.
 * </p>
 *
 * <p>
 * Runtime conversation behaviour is not hardcoded here.
 * The selected Flow and its configured nodes are resolved by
 * the Conversation Orchestrator and Flow Runtime.
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

    private final VoiceGatewayCallContextService callContextService;

    private final ConversationOrchestratorService
            conversationOrchestratorService;

    private final SttRuntimeService sttRuntimeService;

    private final VoiceGatewayWebSocketSessionRegistry
            webSocketSessionRegistry;

    private final TtsAudioStreamRegistry ttsAudioStreamRegistry;

    private final RuntimePersistenceService
            runtimePersistenceService;

    /**
     * Stores whether caller speech has been detected by VAD
     * and is waiting for STT confirmation.
     *
     * <p>
     * VAD speech-start alone must not interrupt TTS because
     * background noise or other sounds can trigger VAD.
     * </p>
     */
    private final Map<String, Boolean>
            pendingBargeIns =
            new ConcurrentHashMap<>();

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

        validateStartRequest(request);

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

        validateRuntimeContext(callSession);

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

        registerTtsListener(
                request.getCallId(),
                request.getStreamId()
        );

        /*
         * Reset interruption state before the conversation starts.
         */
        ttsAudioStreamRegistry.resetInterruption(
                request.getCallId()
        );

        /*
         * Reset pending barge-in state.
         */
        pendingBargeIns.remove(
                request.getCallId()
        );

        startSttStreaming(
                request,
                language
        );

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
                orchestratorResponse;

        try {

            orchestratorResponse =
                    conversationOrchestratorService.start(
                            conversationRequest
                    );

        } catch (Exception exception) {

            log.error(
                    "{} Conversation initialization failed. " +
                            "callId={}, flowPublicId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId(),
                    flowPublicId,
                    exception
            );

            stopRuntimeResources(
                    request.getCallId()
            );

            throw exception;
        }

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
    // TTS REGISTRATION
    // =========================================================

    /**
     * Registers the TTS audio listener for the active call.
     *
     * @param callId application Call ID
     * @param streamId provider stream ID
     */
    private void registerTtsListener(
            String callId,
            String streamId) {

        ttsAudioStreamRegistry.register(
                callId,
                (audioBytes, contentType) ->
                        webSocketSessionRegistry.sendAudio(
                                callId,
                                streamId,
                                audioBytes,
                                contentType
                        )
        );

        log.debug(
                "{} TTS audio listener registered. " +
                        "callId={}, streamId={}",
                VoiceGatewayConstants.LOG_PREFIX,
                callId,
                streamId
        );
    }

    // =========================================================
    // STT INITIALIZATION
    // =========================================================

    /**
     * Starts the streaming STT session for the call.
     *
     * @param request Voice Gateway start request
     * @param language configured conversation language
     */
    private void startSttStreaming(
            VoiceGatewayStartRequestDto request,
            String language) {

        try {

            Integer sampleRate =
                    request.getSampleRate() != null
                            ? request.getSampleRate()
                            : VoiceGatewayConstants.AUDIO_SAMPLE_RATE;

            String audioEncoding =
                    request.getAudioEncoding() != null
                            && !request.getAudioEncoding().isBlank()
                            ? request.getAudioEncoding()
                            : VoiceGatewayConstants.AUDIO_ENCODING;

            sttRuntimeService.startStreaming(
                    request.getCallId(),
                    "auto",
                    sampleRate,
                    audioEncoding,
                    buildSttStreamingListener(
                            request.getCallId(),
                            request.getStreamId()
                    )
            );

            log.info(
                    "{} Streaming STT session initialized. " +
                            "callId={}, streamId={}, language={}, " +
                            "sampleRate={}, encoding={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId(),
                    request.getStreamId(),
                    language,
                    sampleRate,
                    audioEncoding
            );

        } catch (Exception exception) {

            log.error(
                    "{} Failed to initialize streaming STT. " +
                            "callId={}, streamId={}, language={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId(),
                    request.getStreamId(),
                    language,
                    exception
            );

            ttsAudioStreamRegistry.remove(
                    request.getCallId()
            );

            throw exception;
        }
    }

    // =========================================================
    // MEDIA
    // =========================================================

    /**
     * Processes an incoming media packet.
     *
     * @param request Voice Gateway media request
     * @return Voice Gateway response
     */
    @Override
    public VoiceGatewayResponseDto processMedia(
            VoiceGatewayMediaRequestDto request) {

        validateMediaRequest(request);

        log.debug(
                "{} Processing incoming media. " +
                        "callId={}, streamId={}, sequenceNumber={}, chunk={}",
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
                    "{} Empty decoded audio received. callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId()
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_AUDIO_PAYLOAD
            );
        }

        log.debug(
                "{} Streaming audio chunk to STT. " +
                        "callId={}, streamId={}, chunk={}, " +
                        "audioBytes={}, encoding={}, sampleRate={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                request.getStreamId(),
                request.getChunk(),
                audioBytes.length,
                request.getAudioEncoding(),
                request.getSampleRate()
        );

        try {

            sttRuntimeService.streamAudio(
                    request.getCallId(),
                    audioBytes
            );

        } catch (IllegalStateException exception) {

            log.error(
                    "{} Active STT session is unavailable while " +
                            "processing media. callId={}, streamId={}, chunk={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    request.getCallId(),
                    request.getStreamId(),
                    request.getChunk(),
                    exception
            );

            throw exception;
        }

        return null;
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

        validateDtmfRequest(request);

        log.info(
                "{} Processing DTMF. " +
                        "callId={}, streamId={}, digit={}",
                VoiceGatewayConstants.LOG_PREFIX,
                request.getCallId(),
                request.getStreamId(),
                request.getDigit()
        );

        return VoiceGatewayResponseDto.builder()
                .callId(request.getCallId())
                .streamId(request.getStreamId())
                .action(VoiceGatewayConstants.ACTION_LISTEN)
                .listen(true)
                .build();
    }

    // =========================================================
    // STOP STREAM
    // =========================================================

    /**
     * Stops the Voice Gateway stream and releases all runtime
     * resources associated with the call.
     *
     * @param request stop request
     * @return Voice Gateway response
     */
    @Override
    public VoiceGatewayResponseDto stopStream(
            VoiceGatewayStopRequestDto request) {

        validateStopRequest(request);

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

        stopRuntimeResources(
                request.getCallId()
        );

        return VoiceGatewayResponseDto.builder()
                .callId(request.getCallId())
                .streamId(request.getStreamId())
                .action(VoiceGatewayConstants.ACTION_END)
                .endCall(true)
                .build();
    }

    /**
     * Stops STT and removes TTS/WebSocket runtime resources.
     *
     * @param callId application Call ID
     */
    private void stopRuntimeResources(
            String callId) {

        pendingBargeIns.remove(
                callId
        );

        try {

            sttRuntimeService.stopStreaming(
                    callId
            );

        } catch (Exception exception) {

            log.warn(
                    "{} Failed to stop STT runtime cleanly. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    callId,
                    exception
            );
        }

        try {

            ttsAudioStreamRegistry.remove(
                    callId
            );

        } catch (Exception exception) {

            log.warn(
                    "{} Failed to remove TTS runtime state. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    callId,
                    exception
            );
        }

        try {

            webSocketSessionRegistry.remove(
                    callId
            );

        } catch (Exception exception) {

            log.warn(
                    "{} Failed to remove WebSocket runtime state. " +
                            "callId={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    callId,
                    exception
            );
        }
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

        return processBargeIn(
                callId,
                null
        );
    }

    /**
     * Processes caller barge-in for a specific telephony stream.
     *
     * @param callId application Call ID
     * @param streamId provider stream ID
     * @return Voice Gateway response
     */
    public VoiceGatewayResponseDto processBargeIn(
            String callId,
            String streamId) {

        if (callId == null
                || callId.isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }

        log.info(
                "{} Confirmed caller barge-in. " +
                        "callId={}, streamId={}",
                VoiceGatewayConstants.LOG_PREFIX,
                callId,
                streamId
        );

        /*
         * Interrupt currently generated TTS.
         */
        ttsAudioStreamRegistry.interrupt(
                callId
        );

        /*
         * Clear audio already queued on the provider side.
         */
        if (streamId != null
                && !streamId.isBlank()) {

            webSocketSessionRegistry.clearAudio(
                    callId,
                    streamId
            );
        }

        return VoiceGatewayResponseDto.builder()
                .callId(callId)
                .streamId(streamId)
                .action(VoiceGatewayConstants.ACTION_LISTEN)
                .listen(true)
                .clearAudio(true)
                .build();
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    /**
     * Converts the Conversation Orchestrator response into
     * a provider-neutral Voice Gateway response.
     *
     * @param callId application Call ID
     * @param streamId provider stream ID
     * @param response conversation orchestrator response
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
                    .callId(callId)
                    .streamId(streamId)
                    .action(VoiceGatewayConstants.ACTION_LISTEN)
                    .listen(true)
                    .build();
        }

        String action =
                response.getAction();

        String audioBase64 =
                response.getAudioBase64();

        String audioContentType =
                response.getAudioContentType();

        String responseText =
                response.getResponseText();

        boolean hasAudio =
                audioBase64 != null
                        && !audioBase64.isBlank();

        boolean completed =
                response.isCompleted();

        boolean transferred =
                response.isTransferred();

        boolean liveTtsStreaming =
                ttsAudioStreamRegistry.getListener(
                        callId
                ) != null;

        if (liveTtsStreaming) {

            audioBase64 = null;
            hasAudio = false;
        }

        log.info(
                "{} Building Voice Gateway response. " +
                        "callId={}, streamId={}, action={}, " +
                        "audioPresent={}, liveTtsStreaming={}, " +
                        "responseTextPresent={}, completed={}, transferred={}",
                VoiceGatewayConstants.LOG_PREFIX,
                callId,
                streamId,
                action,
                hasAudio,
                liveTtsStreaming,
                responseText != null
                        && !responseText.isBlank(),
                completed,
                transferred
        );

        VoiceGatewayResponseDto.VoiceGatewayResponseDtoBuilder
                builder =
                VoiceGatewayResponseDto.builder()
                        .callId(callId)
                        .streamId(streamId)
                        .action(action)
                        .responseText(responseText)
                        .audioBase64(audioBase64)
                        .contentType(audioContentType)
                        .audioEncoding(
                                VoiceGatewayConstants.AUDIO_ENCODING
                        )
                        .sampleRate(
                                VoiceGatewayConstants.AUDIO_SAMPLE_RATE
                        )
                        .channels(
                                VoiceGatewayConstants.AUDIO_CHANNELS
                        );

        if (completed) {

            builder
                    .endCall(true)
                    .listen(false);

        } else if (transferred) {

            builder
                    .transfer(true)
                    .listen(false);

        } else if (hasAudio) {

            builder
                    .listen(false);

        } else {

            builder
                    .listen(true);
        }

        return builder.build();
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
                    .decode(audioBase64);

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

        if (callSession.getFlowPublicId() == null
                || callSession.getFlowPublicId().isBlank()) {

            log.error(
                    "{} Flow public ID missing from Call Session. " +
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

    /**
     * Validates the START event.
     *
     * @param request start request
     */
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

    /**
     * Validates the MEDIA event.
     *
     * @param request media request
     */
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

    /**
     * Validates DTMF input.
     *
     * @param request DTMF request
     */
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

    /**
     * Validates the STOP event.
     *
     * @param request stop request
     */
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

    // =========================================================
    // STT LISTENER
    // =========================================================

    /**
     * Creates the listener for the active STT session.
     *
     * <p>
     * VAD speech-start events are treated only as possible
     * caller speech. TTS interruption happens after STT
     * provides actual transcript content.
     * </p>
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     * @return STT streaming listener
     */
    private SttStreamingListener
    buildSttStreamingListener(
            String callId,
            String streamId) {

        return new SttStreamingListener() {

            /**
             * Handles partial STT transcription.
             *
             * <p>
             * A partial transcript confirms that the audio
             * detected by VAD contains speech recognized by
             * the STT engine. Only then is TTS interrupted.
             * </p>
             *
             * @param partialCallId application call identifier
             * @param transcript partial transcript
             */
            @Override
            public void onPartialTranscript(
                    String partialCallId,
                    String transcript) {

                if (transcript == null
                        || transcript.isBlank()) {

                    return;
                }

                /*
                 * Do not process barge-in when the agent is not
                 * currently speaking.
                 */
                if (!ttsAudioStreamRegistry.isPlaybackActive(
                        partialCallId
                )) {

                    pendingBargeIns.remove(
                            partialCallId
                    );

                    log.debug(
                            "{} Ignoring partial transcript because TTS " +
                                    "playback is not active. callId={}, transcript={}",
                            VoiceGatewayConstants.LOG_PREFIX,
                            partialCallId,
                            transcript
                    );

                    return;
                }

                /*
                 * Ignore very short/noisy STT partials.
                 */
                if (!isMeaningfulBargeInTranscript(
                        transcript
                )) {

                    log.debug(
                            "{} Ignoring weak partial transcript for barge-in. " +
                                    "callId={}, transcript={}",
                            VoiceGatewayConstants.LOG_PREFIX,
                            partialCallId,
                            transcript
                    );

                    return;
                }

                log.debug(
                        "{} Partial STT transcript received. " +
                                "callId={}, transcript={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        partialCallId,
                        transcript
                );

                Boolean pending =
                        pendingBargeIns.get(
                                partialCallId
                        );

                if (!Boolean.TRUE.equals(pending)) {

                    return;
                }

                /*
                 * STT has now confirmed meaningful caller speech.
                 */
                pendingBargeIns.remove(
                        partialCallId
                );

                log.info(
                        "{} Caller speech confirmed by STT. " +
                                "Processing TTS barge-in. " +
                                "callId={}, transcript={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        partialCallId,
                        transcript
                );

                processBargeIn(
                        partialCallId,
                        streamId
                );
            }

            /**
             * Determines whether a partial transcript contains enough
             * meaningful speech to be considered a barge-in.
             *
             * @param transcript partial STT transcript
             * @return {@code true} when the transcript is meaningful
             */
            private boolean isMeaningfulBargeInTranscript(
                    String transcript) {

                if (transcript == null
                        || transcript.isBlank()) {

                    return false;
                }

                String normalizedTranscript =
                        transcript.trim();

                /*
                 * Ignore extremely short STT fragments.
                 */
                if (normalizedTranscript.length() < 3) {
                    return false;
                }

                String[] words =
                        normalizedTranscript.split("\\s+");

                /*
                 * Accept either a multi-word phrase or a sufficiently
                 * long single recognized word.
                 */
                return words.length >= 2
                        || normalizedTranscript.length() >= 8;
            }

            /**
             * Handles final STT transcription.
             *
             * @param finalCallId application call identifier
             * @param transcript final transcript
             */
            /**
             * Handles final STT transcription.
             *
             * <p>
             * The detected language returned by the STT provider is
             * propagated to the conversation Flow so that subsequent
             * AI and TTS nodes can respond in the customer's current
             * language.
             * </p>
             *
             * @param response final STT response
             */
            @Override
            public void onFinalTranscript(
                    SttTranscriptionResponse response) {

                if (response == null) {

                    return;
                }

                String finalCallId =
                        response.getCallId();

                String transcript =
                        response.getTranscript();

                String detectedLanguage =
                        response.getLanguage();

                pendingBargeIns.remove(
                        finalCallId
                );

                if (transcript == null
                        || transcript.isBlank()) {

                    return;
                }

                log.info(
                        "{} Final STT transcript received. " +
                                "callId={}, language={}, transcript={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        finalCallId,
                        detectedLanguage,
                        transcript
                );

                /*
                 * Persist the caller's final transcript before passing
                 * it to the conversation orchestrator.
                 *
                 * This is required for realtime streaming calls because
                 * the streaming STT callback does not go through
                 * RuntimePersistenceService.saveStt().
                 */
                try {

                    runtimePersistenceService.saveTranscriptMessage(
                            finalCallId,
                            "USER",
                            transcript,
                            detectedLanguage,
                            "STT_STREAMING"
                    );

                } catch (Exception exception) {

                    /*
                     * Transcript persistence must not stop the live
                     * conversation from continuing.
                     */
                    log.error(
                            "{} Failed to persist final STT transcript. " +
                                    "callId={}",
                            VoiceGatewayConstants.LOG_PREFIX,
                            finalCallId,
                            exception
                    );
                }

                /*
                 * Continue normal conversation processing.
                 */
                processFinalTranscript(
                        finalCallId,
                        streamId,
                        transcript,
                        detectedLanguage
                );
            }

            /**
             * Handles caller speech start.
             *
             * <p>
             * VAD speech-start is intentionally not treated
             * as a confirmed barge-in. Background noise,
             * music, echo, or another speaker can trigger VAD.
             * </p>
             *
             * @param speechCallId application call identifier
             */
            @Override
            public void onSpeechStart(
                    String speechCallId) {

                pendingBargeIns.put(
                        speechCallId,
                        true
                );

                log.debug(
                        "{} Possible caller speech detected by VAD. " +
                                "Waiting for STT confirmation. callId={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        speechCallId
                );
            }

            /**
             * Handles caller speech end.
             *
             * @param speechCallId application call identifier
             */
            @Override
            public void onSpeechEnd(
                    String speechCallId) {

                /*
                 * If no transcript was produced while the speech
                 * segment was active, consider it noise/background
                 * audio and discard the pending barge-in.
                 */
                pendingBargeIns.remove(
                        speechCallId
                );

                log.debug(
                        "{} Caller speech segment ended without " +
                                "STT-confirmed barge-in. callId={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        speechCallId
                );
            }

            /**
             * Handles streaming STT errors.
             *
             * @param failedCallId application call identifier
             * @param exception streaming exception
             */
            @Override
            public void onError(
                    String failedCallId,
                    Throwable exception) {

                pendingBargeIns.remove(
                        failedCallId
                );

                log.error(
                        "{} Streaming STT failed. callId={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        failedCallId,
                        exception
                );
            }
        };
    }

    // =========================================================
    // FINAL TRANSCRIPT
    // =========================================================

    /**
     * Continues the conversation after a final STT transcript.
     *
     * @param callId application Call ID
     * @param streamId provider stream identifier
     * @param transcript final transcript
     * @param language detected language
     */
    private void processFinalTranscript(
            String callId,
            String streamId,
            String transcript,
            String language) {

        try {

            ProcessTranscriptRequestDto request =
                    ProcessTranscriptRequestDto
                            .builder()
                            .callId(callId)
                            .transcript(transcript)
                            .language(language)
                            .finalTranscript(true)
                            .build();

            ConversationOrchestratorResponseDto
                    orchestratorResponse =
                    conversationOrchestratorService
                            .processTranscript(
                                    request
                            );

            VoiceGatewayResponseDto
                    gatewayResponse =
                    buildResponse(
                            callId,
                            streamId,
                            orchestratorResponse
                    );

            webSocketSessionRegistry.send(
                    callId,
                    gatewayResponse
            );

        } catch (Exception exception) {

            log.error(
                    "{} Failed to process final STT transcript. " +
                            "callId={}, transcript={}",
                    VoiceGatewayConstants.LOG_PREFIX,
                    callId,
                    transcript,
                    exception
            );
        }
    }
}