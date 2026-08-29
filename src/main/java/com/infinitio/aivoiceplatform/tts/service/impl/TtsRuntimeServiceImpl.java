package com.infinitio.aivoiceplatform.tts.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.runtimepersistence.RuntimePersistenceService;
import com.infinitio.aivoiceplatform.tts.config.TtsProperties;
import com.infinitio.aivoiceplatform.tts.constant.TtsMessages;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsAudioStorageResponse;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.provider.TtsProvider;
import com.infinitio.aivoiceplatform.tts.service.TtsAudioStorageService;
import com.infinitio.aivoiceplatform.tts.service.TtsRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements runtime text-to-speech business operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TtsRuntimeServiceImpl
        implements TtsRuntimeService {

    private final TtsProvider ttsProvider;

    private final TtsProperties ttsProperties;

    private final TtsAudioStorageService
            ttsAudioStorageService;

    private final RuntimePersistenceService
            runtimePersistenceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public TtsSynthesisResponse synthesize(
            TtsSynthesisRequest request) {

        validateRequest(request);

        log.info(
                "Starting TTS synthesis. callId={}, language={}, speaker={}, textLength={}, finalResponse={}",
                request.getCallId(),
                request.getLanguage(),
                request.getSpeaker(),
                request.getText().length(),
                request.isFinalResponse()
        );

        long startTime =
                System.currentTimeMillis();

        try {

            validateLanguage(
                    request.getLanguage()
            );

            validateSpeaker(
                    request.getSpeaker()
            );

            validateTextSize(
                    request.getText()
            );

            validateProvider();

            TtsSynthesisResponse response =
                    ttsProvider.synthesize(request);

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            if (response == null) {

                log.error(
                        "TTS provider returned an empty response. callId={}, latencyMs={}",
                        request.getCallId(),
                        latencyMs
                );

                throw new IllegalStateException(
                        TtsMessages.SYNTHESIS_FAILED
                );
            }

            if (response.getAudioBase64() == null
                    || response.getAudioBase64().isBlank()) {

                log.error(
                        "TTS provider returned empty Base64 audio. callId={}",
                        request.getCallId()
                );

                throw new IllegalStateException(
                        TtsMessages.AUDIO_EMPTY
                );
            }

            byte[] audioBytes =
                    response.getAudioBytes();

            if (audioBytes == null
                    || audioBytes.length == 0) {

                log.error(
                        "TTS provider returned empty raw audio bytes. callId={}",
                        request.getCallId()
                );

                throw new IllegalStateException(
                        TtsMessages.AUDIO_EMPTY
                );
            }

            log.info(
                    "Storing generated TTS audio. callId={}, contentType={}, audioSizeBytes={}",
                    request.getCallId(),
                    response.getContentType(),
                    audioBytes.length
            );

            TtsAudioStorageResponse
                    storageResponse =
                    ttsAudioStorageService.store(
                            audioBytes,
                            response.getContentType(),
                            request.getCallId()
                    );

            response.setAudioUrl(
                    storageResponse.getAudioUrl()
            );

            response.setFileName(
                    storageResponse.getFileName()
            );

            response.setFilePath(
                    storageResponse.getFilePath()
            );

            /*
             * Raw audio bytes are required only during
             * internal file storage.
             */
            response.setAudioBytes(null);

            /*
             * Provider latency is retained when available.
             * Runtime latency is fallback.
             */
            if (response.getLatencyMs() == null) {

                response.setLatencyMs(
                        latencyMs
                );
            }

            /*
             * Persist TTS interaction.
             */
            runtimePersistenceService.saveTts(
                    request,
                    response
            );

            log.info(
                    "TTS synthesis completed successfully. callId={}, provider={}, model={}, speaker={}, language={}, latencyMs={}, inputCharacters={}, fileName={}, audioUrl={}",
                    request.getCallId(),
                    response.getProvider(),
                    response.getModel(),
                    response.getSpeaker(),
                    response.getLanguage(),
                    response.getLatencyMs(),
                    response.getInputCharacters(),
                    response.getFileName(),
                    response.getAudioUrl()
            );

            return response;

        } catch (BadRequestException exception) {

            log.warn(
                    "TTS synthesis validation failed. callId={}, speaker={}, language={}, reason={}",
                    request.getCallId(),
                    request.getSpeaker(),
                    request.getLanguage(),
                    exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "TTS synthesis failed. callId={}, provider={}, speaker={}, language={}, latencyMs={}",
                    request.getCallId(),
                    getProviderCode(),
                    request.getSpeaker(),
                    request.getLanguage(),
                    latencyMs,
                    exception
            );

            throw new IllegalStateException(
                    TtsMessages.SYNTHESIS_FAILED,
                    exception
            );
        }
    }

    /**
     * Validates TTS synthesis request.
     */
    private void validateRequest(
            TtsSynthesisRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    TtsMessages.SYNTHESIS_REQUEST_REQUIRED
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new BadRequestException(
                    TtsMessages.CALL_ID_REQUIRED
            );
        }

        if (request.getLanguage() == null
                || request.getLanguage().isBlank()) {

            throw new BadRequestException(
                    TtsMessages.LANGUAGE_REQUIRED
            );
        }

        if (request.getSpeaker() == null
                || request.getSpeaker().isBlank()) {

            throw new BadRequestException(
                    TtsMessages.SPEAKER_REQUIRED
            );
        }

        if (request.getText() == null
                || request.getText().isBlank()) {

            throw new BadRequestException(
                    TtsMessages.TEXT_REQUIRED
            );
        }
    }

    /**
     * Validates requested language.
     */
    private void validateLanguage(
            String language) {

        List<String> supportedLanguages =
                ttsProperties
                        .getSupportedLanguages();

        if (supportedLanguages == null
                || supportedLanguages.isEmpty()) {

            log.warn(
                    "No TTS supported languages are configured."
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
                    "Unsupported TTS language requested. language={}",
                    language
            );

            throw new BadRequestException(
                    TtsMessages.LANGUAGE_NOT_SUPPORTED
            );
        }
    }

    /**
     * Validates requested speaker.
     *
     * TtsProperties defines supportedSpeakers as:
     *
     * Map<String, List<String>>
     *
     * where the key can represent a speaker group/gender.
     */
    private void validateSpeaker(
            String speaker) {

        Map<String, List<String>> supportedSpeakers =
                ttsProperties
                        .getSupportedSpeakers();

        if (supportedSpeakers == null
                || supportedSpeakers.isEmpty()) {

            log.warn(
                    "No TTS supported speakers are configured."
            );

            return;
        }

        boolean supported =
                supportedSpeakers.values()
                        .stream()
                        .filter(
                                speakers ->
                                        speakers != null
                        )
                        .flatMap(
                                List::stream
                        )
                        .filter(
                                configuredSpeaker ->
                                        configuredSpeaker != null
                        )
                        .anyMatch(
                                configuredSpeaker ->
                                        configuredSpeaker
                                                .equals(
                                                        speaker
                                                )
                        );

        if (!supported) {

            log.warn(
                    "Unsupported TTS speaker requested. speaker={}",
                    speaker
            );

            throw new BadRequestException(
                    TtsMessages.SPEAKER_NOT_SUPPORTED
            );
        }
    }

    /**
     * Validates configured maximum TTS text size.
     *
     * IMPORTANT:
     * TtsProperties contains maxTextCharacters,
     * therefore Lombok generates:
     *
     * getMaxTextCharacters()
     */
    private void validateTextSize(
            String text) {

        Integer maxTextCharacters =
                ttsProperties
                        .getMaxTextCharacters();

        if (maxTextCharacters == null) {

            log.warn(
                    "Maximum TTS text size is not configured."
            );

            return;
        }

        if (text.length()
                > maxTextCharacters) {

            log.warn(
                    "TTS text size exceeds configured limit. textLength={}, maxCharacters={}",
                    text.length(),
                    maxTextCharacters
            );

            throw new BadRequestException(
                    TtsMessages.TEXT_SIZE_EXCEEDED
            );
        }
    }

    /**
     * Validates configured TTS provider.
     */
    private void validateProvider() {

        if (ttsProvider == null) {

            log.error(
                    "TTS provider is not configured."
            );

            throw new IllegalStateException(
                    TtsMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        if (!ttsProvider.isAvailable()) {

            log.error(
                    "Configured TTS provider is unavailable. provider={}",
                    getProviderCode()
            );

            throw new IllegalStateException(
                    TtsMessages.PROVIDER_UNAVAILABLE
            );
        }
    }

    /**
     * Returns provider code for logging.
     */
    private String getProviderCode() {

        if (ttsProvider == null) {

            return "unknown";
        }

        try {

            return ttsProvider.getProviderCode();

        } catch (Exception exception) {

            log.warn(
                    "Unable to determine TTS provider code.",
                    exception
            );

            return "unknown";
        }
    }
}