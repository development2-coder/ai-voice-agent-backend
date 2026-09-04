package com.infinitio.aivoiceplatform.stt.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.runtimepersistence.RuntimePersistenceService;
import com.infinitio.aivoiceplatform.stt.config.SttProperties;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.provider.SttProvider;
import com.infinitio.aivoiceplatform.stt.provider.SttStreamingListener;
import com.infinitio.aivoiceplatform.stt.provider.SttStreamingSession;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements runtime speech-to-text business operations.
 *
 * <p>
 * This service is responsible for validating runtime STT requests,
 * validating configured STT capabilities, delegating transcription
 * to the configured provider and persisting runtime transcription
 * results.
 * </p>
 *
 * <p>
 * For realtime transcription, one streaming STT session is maintained
 * for each active application call.
 * </p>
 *
 * <p>
 * Provider-specific URLs, API keys, models and runtime values are
 * obtained from {@link SttProperties} and are never hardcoded in
 * this service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SttRuntimeServiceImpl
        implements SttRuntimeService {

    /**
     * Configured runtime STT provider.
     */
    private final SttProvider sttProvider;

    /**
     * Runtime STT configuration.
     */
    private final SttProperties sttProperties;

    /**
     * Runtime persistence service.
     */
    private final RuntimePersistenceService
            runtimePersistenceService;

    /**
     * Active realtime STT sessions.
     *
     * <p>
     * The key is the application call identifier.
     * </p>
     */
    private final Map<String, SttStreamingSession>
            streamingSessions =
            new ConcurrentHashMap<>();

    /**
     * Performs synchronous speech-to-text transcription.
     *
     * @param request STT transcription request
     * @return transcription response
     */
    @Override
    public SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request) {

        validateRequest(
                request
        );

        validateLanguage(
                request.getLanguage()
        );

        validateAudioSize(
                request.getAudio()
        );

        validateProvider();

        log.info(
                "Starting STT transcription. " +
                        "callId={}, provider={}, language={}, " +
                        "contentType={}, fileName={}, " +
                        "audioSizeBytes={}, finalTranscript={}",
                request.getCallId(),
                sttProvider.getProviderCode(),
                request.getLanguage(),
                request.getContentType(),
                request.getFileName(),
                request.getAudio().length,
                request.isFinalTranscript()
        );

        long startTime =
                System.currentTimeMillis();

        try {

            SttTranscriptionResponse response =
                    sttProvider.transcribe(
                            request
                    );

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            if (response == null) {

                log.error(
                        "STT provider returned an empty response. " +
                                "callId={}, provider={}, " +
                                "latencyMs={}",
                        request.getCallId(),
                        sttProvider.getProviderCode(),
                        latencyMs
                );

                throw new IllegalStateException(
                        SttMessages.TRANSCRIPTION_FAILED
                );
            }

            enrichResponse(
                    request,
                    response,
                    latencyMs
            );

            log.info(
                    "STT transcription completed successfully. " +
                            "callId={}, provider={}, language={}, " +
                            "latencyMs={}, finalTranscript={}",
                    response.getCallId(),
                    response.getProvider(),
                    response.getLanguage(),
                    response.getLatencyMs(),
                    response.isFinalTranscript()
            );

            /*
             * Persist the actual runtime transcription result.
             *
             * This does not modify the STT configuration.
             */
            runtimePersistenceService.saveStt(
                    request,
                    response
            );

            return response;

        } catch (BadRequestException exception) {

            log.warn(
                    "STT transcription validation failed. " +
                            "callId={}, reason={}",
                    request.getCallId(),
                    exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "STT transcription failed. " +
                            "callId={}, provider={}, " +
                            "latencyMs={}",
                    request.getCallId(),
                    sttProvider.getProviderCode(),
                    latencyMs,
                    exception
            );

            throw new IllegalStateException(
                    SttMessages.TRANSCRIPTION_FAILED,
                    exception
            );
        }
    }

    /**
     * Starts a realtime STT streaming session.
     *
     * @param callId application call identifier
     * @param language conversation language
     * @param sampleRate audio sample rate
     * @param audioEncoding audio encoding
     * @param listener streaming result listener
     */
    @Override
    public void startStreaming(
            String callId,
            String language,
            Integer sampleRate,
            String audioEncoding,
            SttStreamingListener listener) {

        validateStreamingRequest(
                callId,
                language,
                sampleRate,
                audioEncoding,
                listener
        );

        validateLanguage(
                language
        );

        validateProvider();

        /*
         * A call must have only one active STT streaming session.
         * Close the previous session before replacing it.
         */
        stopStreaming(
                callId
        );

        log.info(
                "Starting realtime STT streaming session. " +
                        "callId={}, provider={}, language={}, " +
                        "sampleRate={}, audioEncoding={}",
                callId,
                sttProvider.getProviderCode(),
                language,
                sampleRate,
                audioEncoding
        );

        try {

            SttStreamingSession session =
                    sttProvider.openStreamingSession(
                            callId,
                            language,
                            sampleRate,
                            audioEncoding,
                            listener
                    );

            if (session == null) {

                log.error(
                        "STT provider returned a null streaming " +
                                "session. callId={}, provider={}",
                        callId,
                        sttProvider.getProviderCode()
                );

                throw new IllegalStateException(
                        SttMessages.STREAMING_SESSION_NOT_OPEN
                );
            }

            /*
             * Store the session only after the provider successfully
             * creates the streaming session.
             */
            streamingSessions.put(
                    callId,
                    session
            );

            log.info(
                    "Realtime STT streaming session created. " +
                            "callId={}, provider={}, open={}",
                    callId,
                    sttProvider.getProviderCode(),
                    session.isOpen()
            );

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Unable to start realtime STT streaming. " +
                            "callId={}, provider={}, language={}, " +
                            "sampleRate={}, audioEncoding={}",
                    callId,
                    sttProvider.getProviderCode(),
                    language,
                    sampleRate,
                    audioEncoding,
                    exception
            );

            throw new IllegalStateException(
                    SttMessages.STREAMING_START_FAILED,
                    exception
            );
        }
    }

    /**
     * Sends an audio chunk to the active realtime STT session.
     *
     * @param callId application call identifier
     * @param audio audio chunk
     */
    @Override
    public void streamAudio(
            String callId,
            byte[] audio) {

        validateCallId(
                callId
        );

        if (audio == null
                || audio.length == 0) {

            log.debug(
                    "Ignoring empty STT audio chunk. callId={}",
                    callId
            );

            return;
        }

        validateAudioSize(
                audio
        );

        SttStreamingSession session =
                streamingSessions.get(
                        callId
                );

        if (session == null) {

            log.warn(
                    "No active realtime STT session found. " +
                            "callId={}, audioSizeBytes={}",
                    callId,
                    audio.length
            );

            throw new IllegalStateException(
                    SttMessages.STREAMING_SESSION_NOT_ACTIVE
            );
        }

        if (!session.isOpen()) {

            log.warn(
                    "Realtime STT session is closed. " +
                            "callId={}",
                    callId
            );

            removeAndCloseSession(
                    callId,
                    session
            );

            throw new IllegalStateException(
                    SttMessages.STREAMING_SESSION_CLOSED
            );
        }

        try {

            log.debug(
                    "Forwarding audio chunk to realtime STT. " +
                            "callId={}, audioSizeBytes={}",
                    callId,
                    audio.length
            );

            session.sendAudio(
                    audio
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to send audio to realtime STT provider. " +
                            "callId={}, audioSizeBytes={}",
                    callId,
                    audio.length,
                    exception
            );

            removeAndCloseSession(
                    callId,
                    session
            );

            throw new IllegalStateException(
                    SttMessages.STREAMING_AUDIO_FAILED,
                    exception
            );
        }
    }

    /**
     * Signals the end of the current conversational turn.
     *
     * @param callId application call identifier
     */
    @Override
    public void finishStreamingTurn(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            log.debug(
                    "Ignoring STT turn completion because " +
                            "callId is missing."
            );

            return;
        }

        SttStreamingSession session =
                streamingSessions.get(
                        callId
                );

        if (session == null) {

            log.debug(
                    "No realtime STT session found for turn " +
                            "completion. callId={}",
                    callId
            );

            return;
        }

        if (!session.isOpen()) {

            log.debug(
                    "Realtime STT session is already closed. " +
                            "callId={}",
                    callId
            );

            return;
        }

        try {

            log.debug(
                    "Finishing realtime STT turn. callId={}",
                    callId
            );

            session.finishTurn();

        } catch (Exception exception) {

            log.warn(
                    "Unable to finish realtime STT turn. " +
                            "callId={}",
                    callId,
                    exception
            );
        }
    }

    /**
     * Stops and removes the realtime STT session for a call.
     *
     * @param callId application call identifier
     */
    @Override
    public void stopStreaming(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        SttStreamingSession session =
                streamingSessions.remove(
                        callId
                );

        if (session == null) {

            log.debug(
                    "No realtime STT session found to stop. " +
                            "callId={}",
                    callId
            );

            return;
        }

        log.info(
                "Stopping realtime STT session. callId={}",
                callId
        );

        try {

            session.close();

        } catch (Exception exception) {

            log.warn(
                    "Error while closing realtime STT session. " +
                            "callId={}",
                    callId,
                    exception
            );
        }
    }

    /**
     * Validates a synchronous STT request.
     *
     * <p>
     * Provider and model are intentionally not required here.
     * Provider-specific runtime configuration is resolved from
     * {@link SttProperties}.
     * </p>
     *
     * @param request STT request
     */
    private void validateRequest(
            SttTranscriptionRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    SttMessages.TRANSCRIPTION_REQUEST_REQUIRED
            );
        }

        validateCallId(
                request.getCallId()
        );

        if (request.getAudio() == null
                || request.getAudio().length == 0) {

            throw new BadRequestException(
                    SttMessages.AUDIO_REQUIRED
            );
        }

        if (request.getLanguage() == null
                || request.getLanguage().isBlank()) {

            throw new BadRequestException(
                    SttMessages.LANGUAGE_REQUIRED
            );
        }
    }

    /**
     * Validates realtime STT streaming parameters.
     *
     * @param callId application call identifier
     * @param language conversation language
     * @param sampleRate audio sample rate
     * @param audioEncoding audio encoding
     * @param listener streaming listener
     */
    private void validateStreamingRequest(
            String callId,
            String language,
            Integer sampleRate,
            String audioEncoding,
            SttStreamingListener listener) {

        validateCallId(
                callId
        );

        if (language == null
                || language.isBlank()) {

            throw new BadRequestException(
                    SttMessages.LANGUAGE_REQUIRED
            );
        }

        if (sampleRate == null) {

            log.warn(
                    "STT streaming sample rate is missing. " +
                            "callId={}",
                    callId
            );

            throw new BadRequestException(
                    SttMessages.STREAMING_SAMPLE_RATE_REQUIRED
            );
        }

        if (sampleRate <= 0) {

            log.warn(
                    "Invalid STT streaming sample rate. " +
                            "callId={}, sampleRate={}",
                    callId,
                    sampleRate
            );

            throw new BadRequestException(
                    SttMessages.STREAMING_SAMPLE_RATE_INVALID
            );
        }

        if (audioEncoding == null
                || audioEncoding.isBlank()) {

            log.warn(
                    "STT streaming audio encoding is missing. " +
                            "callId={}",
                    callId
            );

            throw new BadRequestException(
                    SttMessages.STREAMING_AUDIO_ENCODING_REQUIRED
            );
        }

        if (listener == null) {

            log.warn(
                    "STT streaming listener is missing. " +
                            "callId={}",
                    callId
            );

            throw new BadRequestException(
                    SttMessages.STREAMING_LISTENER_REQUIRED
            );
        }
    }

    /**
     * Validates an application call identifier.
     *
     * @param callId application call identifier
     */
    private void validateCallId(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            throw new BadRequestException(
                    SttMessages.CALL_ID_REQUIRED
            );
        }
    }

    /**
     * Validates whether a language is configured as supported.
     *
     * @param language requested language
     */
    private void validateLanguage(
            String language) {

        List<String> supportedLanguages =
                sttProperties.getSupportedLanguages();

        if (supportedLanguages == null
                || supportedLanguages.isEmpty()) {

            log.warn(
                    "No STT supported languages are configured."
            );

            return;
        }

        boolean supported =
                supportedLanguages.stream()
                        .filter(
                                configuredLanguage ->
                                        configuredLanguage != null
                        )
                        .map(
                                String::trim
                        )
                        .anyMatch(
                                configuredLanguage ->
                                        configuredLanguage
                                                .equalsIgnoreCase(
                                                        language.trim()
                                                )
                        );

        if (!supported) {

            log.warn(
                    "Unsupported STT language requested. " +
                            "language={}",
                    language
            );

            throw new BadRequestException(
                    SttMessages.LANGUAGE_NOT_SUPPORTED
            );
        }
    }

    /**
     * Validates the configured maximum audio size.
     *
     * @param audio audio bytes
     */
    private void validateAudioSize(
            byte[] audio) {

        Long maxAudioSizeBytes =
                sttProperties.getMaxAudioSizeBytes();

        if (maxAudioSizeBytes == null) {

            log.warn(
                    "Maximum STT audio size is not configured."
            );

            return;
        }

        if (maxAudioSizeBytes <= 0) {

            log.error(
                    "Invalid maximum STT audio size configuration. " +
                            "maxAudioSizeBytes={}",
                    maxAudioSizeBytes
            );

            return;
        }

        if (audio.length > maxAudioSizeBytes) {

            log.warn(
                    "STT audio size exceeds configured limit. " +
                            "sizeBytes={}, maxSizeBytes={}",
                    audio.length,
                    maxAudioSizeBytes
            );

            throw new BadRequestException(
                    SttMessages.AUDIO_SIZE_EXCEEDED
            );
        }
    }

    /**
     * Validates the configured runtime STT provider.
     *
     * <p>
     * The provider implementation must match the provider selected
     * through external runtime configuration.
     * </p>
     */
    private void validateProvider() {

        if (sttProvider == null) {

            log.error(
                    "STT runtime provider is not configured."
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        String configuredProvider =
                sttProperties.getProvider();

        if (configuredProvider == null
                || configuredProvider.isBlank()) {

            log.error(
                    "STT provider configuration is missing."
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        String runtimeProvider =
                sttProvider.getProviderCode();

        if (runtimeProvider == null
                || runtimeProvider.isBlank()) {

            log.error(
                    "STT provider returned an empty provider code."
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        if (!configuredProvider.trim()
                .equalsIgnoreCase(
                        runtimeProvider.trim()
                )) {

            log.error(
                    "Configured STT provider does not match " +
                            "runtime provider. configuredProvider={}, " +
                            "runtimeProvider={}",
                    configuredProvider,
                    runtimeProvider
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        if (!sttProvider.isAvailable()) {

            log.error(
                    "Configured STT provider is unavailable. " +
                            "provider={}",
                    runtimeProvider
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_UNAVAILABLE
            );
        }
    }

    /**
     * Enriches a provider response with runtime information.
     *
     * @param request original STT request
     * @param response provider response
     * @param latencyMs runtime latency
     */
    private void enrichResponse(
            SttTranscriptionRequest request,
            SttTranscriptionResponse response,
            long latencyMs) {

        response.setCallId(
                request.getCallId()
        );

        if (response.getLatencyMs() == null) {

            response.setLatencyMs(
                    latencyMs
            );
        }

        if (response.getLanguage() == null
                || response.getLanguage().isBlank()) {

            response.setLanguage(
                    request.getLanguage()
            );
        }

        response.setFinalTranscript(
                request.isFinalTranscript()
        );

        if (response.getProvider() == null
                || response.getProvider().isBlank()) {

            response.setProvider(
                    sttProvider.getProviderCode()
            );
        }
    }

    /**
     * Removes and closes a streaming session.
     *
     * @param callId application call identifier
     * @param session streaming session
     */
    private void removeAndCloseSession(
            String callId,
            SttStreamingSession session) {

        streamingSessions.remove(
                callId,
                session
        );

        try {

            session.close();

        } catch (Exception exception) {

            log.debug(
                    "Unable to close realtime STT session. " +
                            "callId={}",
                    callId,
                    exception
            );
        }
    }
}