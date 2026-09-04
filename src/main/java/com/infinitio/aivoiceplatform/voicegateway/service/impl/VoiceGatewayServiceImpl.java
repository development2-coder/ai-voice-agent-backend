package com.infinitio.aivoiceplatform.voicegateway.service.impl;

import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationOrchestratorService;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;
import com.infinitio.aivoiceplatform.stt.provider.SttStreamingListener;
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

    private final SttRuntimeService
            sttRuntimeService;

    private final VoiceGatewayWebSocketSessionRegistry
            webSocketSessionRegistry;

    private final TtsAudioStreamRegistry
            ttsAudioStreamRegistry;

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

        validateRuntimeContext(
                callSession
        );

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

        /*
         * Register the TTS output listener before starting the
         * STT stream so that TTS audio generated by the Flow
         * Runtime can immediately be sent to the active
         * telephony WebSocket.
         */
        ttsAudioStreamRegistry.register(
                request.getCallId(),
                (audioBytes, contentType) ->
                        webSocketSessionRegistry.sendAudio(
                                request.getCallId(),
                                request.getStreamId(),
                                audioBytes,
                                contentType
                        )
        );

        /*
         * Reset any stale interruption state from a previous
         * lifecycle using the same Call ID.
         */
        ttsAudioStreamRegistry.resetInterruption(
                request.getCallId()
        );

        sttRuntimeService.startStreaming(
                request.getCallId(),
                language,
                VoiceGatewayConstants.AUDIO_SAMPLE_RATE,
                VoiceGatewayConstants.AUDIO_ENCODING,
                buildSttStreamingListener(
                        request.getCallId(),
                        request.getStreamId()
                )
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
     * Processes an incoming media packet and streams the
     * decoded audio to the active STT session.
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

        sttRuntimeService.streamAudio(
                request.getCallId(),
                audioBytes
        );

        /*
         * MEDIA packets do not produce a conversational response.
         * STT events arrive asynchronously through the listener.
         */
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

        sttRuntimeService.stopStreaming(
                request.getCallId()
        );

        /*
         * Remove TTS streaming state when the call ends.
         */
        ttsAudioStreamRegistry.remove(
                request.getCallId()
        );

        webSocketSessionRegistry.remove(
                request.getCallId()
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
     * <p>
     * Barge-in interrupts the currently active TTS stream and
     * instructs the telephony transport to clear already queued
     * audio.
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
                "{} Barge-in received. callId={}, streamId={}",
                VoiceGatewayConstants.LOG_PREFIX,
                callId,
                streamId
        );

        /*
         * Mark the active TTS stream as interrupted.
         *
         * TtsNodeHandler checks this state before forwarding
         * every generated audio chunk.
         */
        ttsAudioStreamRegistry.interrupt(
                callId
        );

        /*
         * Clear any TTS audio that has already been queued at
         * the telephony/provider side.
         */
        if (streamId != null
                && !streamId.isBlank()) {

            webSocketSessionRegistry.clearAudio(
                    callId,
                    streamId
            );
        }

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

        /*
         * When live TTS streaming is registered for this call,
         * audio chunks are already being sent directly to the
         * telephony WebSocket.
         *
         * Therefore do not send the same complete audio payload
         * again through the normal Voice Gateway response.
         */
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
                        .callId(
                                callId
                        )
                        .streamId(
                                streamId
                        )
                        .action(
                                action
                        )
                        .responseText(
                                responseText
                        )
                        .audioBase64(
                                audioBase64
                        )
                        .contentType(
                                audioContentType
                        )
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
                    .endCall(
                            true
                    )
                    .listen(
                            false
                    );

        } else if (transferred) {

            builder
                    .transfer(
                            true
                    )
                    .listen(
                            false
                    );

        } else if (hasAudio) {

            builder
                    .listen(
                            false
                    );

        } else {

            builder
                    .listen(
                            true
                    );
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

    // =========================================================
    // STT LISTENER
    // =========================================================

    /**
     * Creates the listener for the active STT session.
     *
     * <p>
     * Final transcripts are forwarded to the Conversation
     * Orchestrator. Speech-start events are used to trigger
     * TTS barge-in handling.
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
             * @param failedCallId application call identifier
             * @param transcript partial transcript
             */
            @Override
            public void onPartialTranscript(
                    String failedCallId,
                    String transcript) {

                if (transcript == null
                        || transcript.isBlank()) {

                    return;
                }

                log.debug(
                        "{} Partial STT transcript received. " +
                                "callId={}, transcript={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        failedCallId,
                        transcript
                );
            }

            /**
             * Handles final STT transcription.
             *
             * @param failedCallId application call identifier
             * @param transcript final transcript
             */
            @Override
            public void onFinalTranscript(
                    String failedCallId,
                    String transcript) {

                if (transcript == null
                        || transcript.isBlank()) {

                    return;
                }

                log.info(
                        "{} Final STT transcript received. " +
                                "callId={}, transcript={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        failedCallId,
                        transcript
                );

                processFinalTranscript(
                        failedCallId,
                        streamId,
                        transcript,
                        null
                );
            }

            /**
             * Handles caller speech start.
             *
             * <p>
             * Sarvam VAD detects that the caller has started
             * speaking. This is the barge-in trigger.
             *
             * @param speechCallId application call identifier
             */
            @Override
            public void onSpeechStart(
                    String speechCallId) {

                log.info(
                        "{} Caller speech started. " +
                                "Processing TTS barge-in. callId={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        speechCallId
                );

                processBargeIn(
                        speechCallId,
                        streamId
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

                log.debug(
                        "{} Caller speech ended. callId={}",
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

                log.error(
                        "{} Streaming STT failed. callId={}",
                        VoiceGatewayConstants.LOG_PREFIX,
                        failedCallId,
                        exception
                );
            }
        };
    }

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
                            .callId(
                                    callId
                            )
                            .transcript(
                                    transcript
                            )
                            .language(
                                    language
                            )
                            .finalTranscript(
                                    true
                            )
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