package com.infinitio.aivoiceplatform.stt.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.runtimepersistence.RuntimePersistenceService;
import com.infinitio.aivoiceplatform.stt.config.SttProperties;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.provider.SttProvider;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements runtime speech-to-text business operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SttRuntimeServiceImpl
        implements SttRuntimeService {

    private final SttProvider sttProvider;

    private final SttProperties sttProperties;

    private final RuntimePersistenceService
            runtimePersistenceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request) {

        validateRequest(request);

        log.info(
                "Starting STT transcription. callId={}, language={}, " +
                        "contentType={}, fileName={}, audioSizeBytes={}, " +
                        "finalTranscript={}",
                request.getCallId(),
                request.getLanguage(),
                request.getContentType(),
                request.getFileName(),
                request.getAudio().length,
                request.isFinalTranscript()
        );

        long startTime =
                System.currentTimeMillis();

        try {

            validateLanguage(
                    request.getLanguage()
            );

            validateAudioSize(
                    request.getAudio()
            );

            validateProvider();

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
                                "callId={}, latencyMs={}",
                        request.getCallId(),
                        latencyMs
                );

                throw new IllegalStateException(
                        SttMessages.TRANSCRIPTION_FAILED
                );
            }

            /*
             * Always retain the request call ID.
             *
             * Provider implementations may or may not populate it.
             */
            response.setCallId(
                    request.getCallId()
            );

            /*
             * Provider latency is preferred.
             * Runtime latency is used as fallback.
             */
            if (response.getLatencyMs() == null) {

                response.setLatencyMs(
                        latencyMs
                );
            }

            /*
             * Provider may not populate language.
             * Use the request language as fallback.
             */
            if (response.getLanguage() == null
                    || response.getLanguage().isBlank()) {

                response.setLanguage(
                        request.getLanguage()
                );
            }

            /*
             * Provider may not populate finalTranscript.
             */
            response.setFinalTranscript(
                    request.isFinalTranscript()
            );

            /*
             * Provider may not populate provider name.
             */
            if (response.getProvider() == null
                    || response.getProvider().isBlank()) {

                response.setProvider(
                        sttProvider.getProviderCode()
                );
            }

            log.info(
                    "STT transcription completed successfully. " +
                            "callId={}, provider={}, language={}, " +
                            "latencyMs={}, finalTranscript={}",
                    request.getCallId(),
                    response.getProvider(),
                    response.getLanguage(),
                    response.getLatencyMs(),
                    response.isFinalTranscript()
            );

            /*
             * Persist the actual runtime STT response.
             *
             * This does NOT modify the stts configuration table.
             * It creates one row in stt_interactions.
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
                            "callId={}, latencyMs={}",
                    request.getCallId(),
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
     * Validates the STT request.
     *
     * @param request STT request
     */
    private void validateRequest(
            SttTranscriptionRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    SttMessages
                            .TRANSCRIPTION_REQUEST_REQUIRED
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new BadRequestException(
                    SttMessages.CALL_ID_REQUIRED
            );
        }

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
     * Validates the requested language.
     *
     * @param language requested language
     */
    private void validateLanguage(
            String language) {

        List<String> supportedLanguages =
                sttProperties
                        .getSupportedLanguages();

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
                        .anyMatch(
                                configuredLanguage ->
                                        configuredLanguage
                                                .equalsIgnoreCase(
                                                        language
                                                )
                        );

        if (!supported) {

            log.warn(
                    "Unsupported STT language requested. language={}",
                    language
            );

            throw new BadRequestException(
                    SttMessages
                            .LANGUAGE_NOT_SUPPORTED
            );
        }
    }

    /**
     * Validates the configured maximum audio size.
     *
     * @param audio audio data
     */
    private void validateAudioSize(
            byte[] audio) {

        Long maxAudioSizeBytes =
                sttProperties
                        .getMaxAudioSizeBytes();

        if (maxAudioSizeBytes == null) {

            log.warn(
                    "Maximum STT audio size is not configured."
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
                    SttMessages
                            .AUDIO_SIZE_EXCEEDED
            );
        }
    }

    /**
     * Validates the configured STT provider.
     */
    private void validateProvider() {

        if (sttProvider == null) {

            log.error(
                    "STT provider is not configured."
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

        if (!configuredProvider.equalsIgnoreCase(
                sttProvider.getProviderCode()
        )) {

            log.error(
                    "Configured STT provider does not match " +
                            "runtime provider. configuredProvider={}, " +
                            "runtimeProvider={}",
                    configuredProvider,
                    sttProvider.getProviderCode()
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        if (!sttProvider.isAvailable()) {

            log.error(
                    "Configured STT provider is unavailable. provider={}",
                    sttProvider.getProviderCode()
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_UNAVAILABLE
            );
        }
    }
}