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
import java.util.ArrayDeque;
import java.util.Deque;
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
     * Audio packets received while a realtime STT session is
     * being established.
     *
     * <p>
     * Exotel can start sending MEDIA packets immediately after
     * the WebSocket START event. Sarvam STT establishment is
     * asynchronous, therefore a small temporary queue prevents
     * the first audio packets from being lost.
     * </p>
     */
    private final Map<String, Deque<byte[]>>
            pendingAudio =
            new ConcurrentHashMap<>();

    /**
     * Maximum number of audio packets retained while STT
     * initialization is in progress.
     *
     * <p>
     * Exotel normally sends very small media packets, therefore
     * this provides a short startup buffer without allowing
     * unbounded memory growth.
     * </p>
     */
    private static final int MAX_PENDING_AUDIO_PACKETS = 100;

    /**
     * Performs synchronous speech-to-text transcription.
     *
     * @param request STT transcription request
     * @return transcription response
     */
    @Override
    public SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request) {

        validateRequest(request);
        validateLanguage(request.getLanguage());
        validateAudioSize(request.getAudio());
        validateProvider();

        log.info(
                "Starting STT transcription. callId={}, provider={}, " +
                        "language={}, audioSizeBytes={}, finalTranscript={}",
                request.getCallId(),
                sttProvider.getProviderCode(),
                request.getLanguage(),
                request.getAudio().length,
                request.isFinalTranscript()
        );

        long startTime = System.currentTimeMillis();

        try {

            SttTranscriptionResponse response =
                    sttProvider.transcribe(request);

            long latencyMs =
                    System.currentTimeMillis() - startTime;

            if (response == null) {

                log.error(
                        "STT provider returned an empty response. " +
                                "callId={}, provider={}",
                        request.getCallId(),
                        sttProvider.getProviderCode()
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

            runtimePersistenceService.saveStt(
                    request,
                    response
            );

            log.info(
                    "STT transcription completed. callId={}, " +
                            "provider={}, latencyMs={}",
                    response.getCallId(),
                    response.getProvider(),
                    response.getLatencyMs()
            );

            return response;

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "STT transcription failed. callId={}, provider={}",
                    request.getCallId(),
                    sttProvider.getProviderCode(),
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
    /**
     * Starts a realtime STT streaming session.
     *
     * <p>
     * The provider session is created before it is registered in the
     * active session map. Exotel can send MEDIA packets concurrently
     * with this operation, therefore packets received before the
     * provider session becomes available are temporarily buffered and
     * flushed after successful initialization.
     * </p>
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

        validateLanguage(language);
        validateProvider();

        stopStreaming(callId);

        pendingAudio.remove(callId);

        log.info(
                "Starting realtime STT session. callId={}, provider={}, " +
                        "language={}, sampleRate={}, encoding={}",
                callId,
                sttProvider.getProviderCode(),
                language,
                sampleRate,
                audioEncoding
        );

        try {

            /*
             * The provider implementation creates the session object
             * and starts its WebSocket connection.
             */
            SttStreamingSession session =
                    sttProvider.openStreamingSession(
                            callId,
                            language,
                            sampleRate,
                            audioEncoding,
                            listener
                    );

            if (session == null) {

                throw new IllegalStateException(
                        SttMessages.STREAMING_SESSION_NOT_OPEN
                );
            }

            /*
             * IMPORTANT:
             *
             * Always register the session here, regardless of whether
             * the provider WebSocket is currently open.
             *
             * streamAudio() can therefore buffer MEDIA packets while
             * the provider connection is being established.
             */
            streamingSessions.put(
                    callId,
                    session
            );

            /*
             * Register the provider-ready callback before checking the
             * current connection state.
             *
             * The Sarvam WebSocket is established asynchronously. Therefore
             * the provider may become ready after this method returns.
             */
            session.setReadyListener(
                    () -> flushPendingAudio(
                            callId,
                            session
                    )
            );

            log.info(
                    "Realtime STT application session registered. " +
                            "callId={}, provider={}, providerSocketOpen={}",
                    callId,
                    sttProvider.getProviderCode(),
                    session.isOpen()
            );

            /*
             * Handle the race where the provider WebSocket became ready
             * before the ready listener was registered.
             */
            if (session.isOpen()) {

                flushPendingAudio(
                        callId,
                        session
                );
            }

        } catch (BadRequestException exception) {

            pendingAudio.remove(callId);

            throw exception;

        } catch (Exception exception) {

            pendingAudio.remove(callId);
            streamingSessions.remove(callId);

            log.error(
                    "Unable to start realtime STT session. " +
                            "callId={}, provider={}",
                    callId,
                    sttProvider.getProviderCode(),
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
    /**
     * Sends an audio chunk to the active realtime STT session.
     *
     * <p>
     * Exotel may send MEDIA immediately after the WebSocket START
     * event while the provider-side Sarvam STT WebSocket is still
     * being established. When no active STT session is available,
     * the audio packet is temporarily buffered instead of causing
     * the Voice Gateway to fail.
     * </p>
     *
     * @param callId application call identifier
     * @param audio audio chunk
     */
    @Override
    public void streamAudio(
            String callId,
            byte[] audio) {

        validateCallId(callId);

        if (audio == null
                || audio.length == 0) {

            return;
        }

        validateAudioSize(audio);

        SttStreamingSession session =
                streamingSessions.get(callId);

        /*
         * This should no longer be the normal startup failure path.
         * It can still occur if MEDIA arrives before startStreaming()
         * creates the application session.
         */
        if (session == null) {

            log.debug(
                    "STT application session not registered yet. " +
                            "Buffering Exotel audio. callId={}, " +
                            "audioSizeBytes={}",
                    callId,
                    audio.length
            );

            bufferPendingAudio(
                    callId,
                    audio
            );

            return;
        }

        /*
         * Provider WebSocket is still connecting.
         */

        boolean sessionOpen =
                session.isOpen();

        log.debug(
                "STT session state before audio forwarding. " +
                        "callId={}, sessionOpen={}, audioSizeBytes={}",
                callId,
                sessionOpen,
                audio.length
        );

        if (!session.isOpen()) {

            log.debug(
                    "STT provider WebSocket is not ready. " +
                            "Buffering Exotel audio. callId={}, " +
                            "audioSizeBytes={}",
                    callId,
                    audio.length
            );

            bufferPendingAudio(
                    callId,
                    audio
            );

            return;
        }

        try {

            session.sendAudio(audio);

        } catch (Exception exception) {

            log.error(
                    "Unable to forward audio to STT provider. " +
                            "callId={}, audioSizeBytes={}",
                    callId,
                    audio.length,
                    exception
            );

            throw new IllegalStateException(
                    SttMessages.STREAMING_AUDIO_FAILED,
                    exception
            );
        }
    }

    /**
     * Buffers one audio packet received before the realtime STT
     * session becomes ready.
     *
     * @param callId application call identifier
     * @param audio audio packet
     */
    private void bufferPendingAudio(
            String callId,
            byte[] audio) {

        Deque<byte[]> queue =
                pendingAudio.computeIfAbsent(
                        callId,
                        key -> new ArrayDeque<>()
                );

        synchronized (queue) {

            if (queue.size() >= MAX_PENDING_AUDIO_PACKETS) {

                queue.pollFirst();

                log.warn(
                        "STT pending audio buffer reached maximum size. " +
                                "Oldest packet removed. callId={}, " +
                                "maxPackets={}",
                        callId,
                        MAX_PENDING_AUDIO_PACKETS
                );
            }

            queue.addLast(
                    audio.clone()
            );

            log.debug(
                    "STT audio buffered. callId={}, queueSize={}, " +
                            "audioSizeBytes={}",
                    callId,
                    queue.size(),
                    audio.length
            );
        }
    }

    /**
     * Flushes audio packets accumulated while the provider
     * WebSocket was connecting.
     *
     * @param callId application call identifier
     * @param session active STT session
     */
    private void flushPendingAudio(
            String callId,
            SttStreamingSession session) {

        if (session == null
                || !session.isOpen()) {

            return;
        }

        Deque<byte[]> queue =
                pendingAudio.remove(callId);

        if (queue == null
                || queue.isEmpty()) {

            return;
        }

        int packetCount = 0;

        synchronized (queue) {

            while (!queue.isEmpty()) {

                byte[] audio =
                        queue.pollFirst();

                if (audio == null
                        || audio.length == 0) {

                    continue;
                }

                try {

                    session.sendAudio(audio);

                    packetCount++;

                } catch (Exception exception) {

                    /*
                     * Put the current packet back so the packet is
                     * not silently lost if provider transmission fails.
                     */
                    queue.addFirst(audio);

                    pendingAudio.put(
                            callId,
                            queue
                    );

                    log.error(
                            "Unable to flush buffered STT audio. " +
                                    "callId={}, flushedPackets={}",
                            callId,
                            packetCount,
                            exception
                    );

                    return;
                }
            }
        }

        log.info(
                "Buffered STT audio flushed. callId={}, packets={}",
                callId,
                packetCount
        );
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

            return;
        }

        SttStreamingSession session =
                streamingSessions.get(callId);

        if (session == null
                || !session.isOpen()) {

            return;
        }

        try {

            session.finishTurn();

        } catch (Exception exception) {

            log.warn(
                    "Unable to finish realtime STT turn. callId={}",
                    callId,
                    exception
            );
        }
    }

    /**
     * Stops the realtime STT session.
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

        pendingAudio.remove(callId);

        SttStreamingSession session =
                streamingSessions.remove(callId);

        if (session == null) {

            return;
        }

        try {

            session.close();

        } catch (Exception exception) {

            log.warn(
                    "Unable to close realtime STT session. callId={}",
                    callId,
                    exception
            );
        }

        log.info(
                "Realtime STT session stopped. callId={}",
                callId
        );
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
     * Validates STT provider configuration.
     */

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

        validateCallId(callId);

        if (language == null
                || language.isBlank()) {

            throw new BadRequestException(
                    SttMessages.LANGUAGE_REQUIRED
            );
        }

        if (sampleRate == null) {

            throw new BadRequestException(
                    SttMessages.STREAMING_SAMPLE_RATE_REQUIRED
            );
        }

        if (sampleRate <= 0) {

            throw new BadRequestException(
                    SttMessages.STREAMING_SAMPLE_RATE_INVALID
            );
        }

        if (audioEncoding == null
                || audioEncoding.isBlank()) {

            throw new BadRequestException(
                    SttMessages.STREAMING_AUDIO_ENCODING_REQUIRED
            );
        }

        if (listener == null) {

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