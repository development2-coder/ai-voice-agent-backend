package com.infinitio.aivoiceplatform.tts.provider;

import java.util.Base64;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.infinitio.aivoiceplatform.tts.config.TtsProperties;
import com.infinitio.aivoiceplatform.tts.constant.TtsMessages;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements Sarvam text-to-speech provider integration.
 *
 * <p>
 * This provider communicates with the Sarvam Bulbul TTS streaming
 * endpoint and converts the returned binary audio stream into Base64
 * for the current runtime response contract.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SarvamTtsProvider implements TtsProvider {

    /**
     * Sarvam provider code.
     */
    private static final String PROVIDER_CODE =
            "sarvam";

    /**
     * Sarvam streaming TTS endpoint.
     */
    private static final String TTS_STREAM_PATH =
            "/text-to-speech/stream";

    /**
     * Default output codec.
     *
     * <p>
     * MP3 is suitable for the current JSON/Base64 runtime response.
     * The value can later be made configurable when the streaming
     * runtime is implemented.
     * </p>
     */
    private static final String OUTPUT_AUDIO_CODEC =
            "mp3";

    private final WebClient sarvamWebClient;

    private final TtsProperties ttsProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderCode() {

        return PROVIDER_CODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TtsSynthesisResponse synthesize(
            TtsSynthesisRequest request) {

        long startTime =
                System.currentTimeMillis();

        log.info(
                "Starting Sarvam TTS synthesis. callId={}, language={}, speaker={}, textLength={}, finalResponse={}",
                request.getCallId(),
                request.getLanguage(),
                request.getSpeaker(),
                request.getText() != null
                        ? request.getText().length()
                        : 0,
                request.isFinalResponse()
        );

        try {

            String language =
                    request.getLanguage().trim();

            String speaker =
                    request.getSpeaker().trim();

            Double pace =
                    request.getPace() != null
                            ? request.getPace()
                            : ttsProperties
                            .getDefaultPace();

            Integer speechSampleRate =
                    request.getSpeechSampleRate() != null
                            ? request
                            .getSpeechSampleRate()
                            : ttsProperties
                            .getDefaultSpeechSampleRate();

            SarvamTtsRequest sarvamRequest =
                    SarvamTtsRequest.builder()
                            .text(
                                    request.getText()
                            )
                            .languageCode(
                                    language
                            )
                            .speaker(
                                    speaker
                            )
                            .model(
                                    ttsProperties
                                            .getModel()
                            )
                            .pace(
                                    pace
                            )
                            .speechSampleRate(
                                    speechSampleRate
                            )
                            .outputAudioCodec(
                                    OUTPUT_AUDIO_CODEC
                            )
                            .build();

            log.info(
                    "Sending TTS request to Sarvam. callId={}, provider={}, model={}, language={}, speaker={}, pace={}, sampleRate={}, codec={}",
                    request.getCallId(),
                    PROVIDER_CODE,
                    ttsProperties.getModel(),
                    language,
                    speaker,
                    pace,
                    speechSampleRate,
                    OUTPUT_AUDIO_CODEC
            );

            byte[] audioBytes =
                    sarvamWebClient
                            .post()
                            .uri(
                                    TTS_STREAM_PATH
                            )
                            .header(
                                    HttpHeaders.CONTENT_TYPE,
                                    MediaType.APPLICATION_JSON_VALUE
                            )
                            .header(
                                    ttsProperties
                                            .getApiKeyHeader(),
                                    ttsProperties
                                            .getApiKey()
                            )
                            .bodyValue(
                                    sarvamRequest
                            )
                            .retrieve()
                            .bodyToFlux(
                                    DataBuffer.class
                            )
                            .map(
                                    dataBuffer -> {

                                        byte[] bytes =
                                                new byte[
                                                        dataBuffer
                                                                .readableByteCount()
                                                        ];

                                        dataBuffer.read(
                                                bytes
                                        );

                                        /*
                                         * Release the DataBuffer after
                                         * copying the bytes to avoid
                                         * memory leaks with Netty.
                                         */
                                        org.springframework.core.io.buffer
                                                .DataBufferUtils
                                                .release(
                                                        dataBuffer
                                                );

                                        return bytes;
                                    }
                            )
                            .collectList()
                            .map(
                                    chunks ->
                                            combineAudioChunks(
                                                    chunks
                                            )
                            )
                            .block();

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            if (audioBytes == null
                    || audioBytes.length == 0) {

                log.error(
                        "Sarvam TTS returned empty audio. callId={}, latencyMs={}",
                        request.getCallId(),
                        latencyMs
                );

                throw new IllegalStateException(
                        TtsMessages
                                .SARVAM_SYNTHESIS_FAILED
                );
            }

            validateAudioSize(
                    audioBytes,
                    request.getCallId()
            );

            String audioBase64 =
                    Base64.getEncoder()
                            .encodeToString(
                                    audioBytes
                            );

            log.info(
                    "Sarvam TTS synthesis completed successfully. callId={}, provider={}, model={}, language={}, speaker={}, audioSizeBytes={}, latencyMs={}",
                    request.getCallId(),
                    PROVIDER_CODE,
                    ttsProperties.getModel(),
                    language,
                    speaker,
                    audioBytes.length,
                    latencyMs
            );

            return TtsSynthesisResponse.builder()
                    .callId(
                            request.getCallId()
                    )
                    .audioBase64(
                            audioBase64
                    )
                    .audioBytes(
                            audioBytes
                    )
                    .contentType(
                            resolveContentType()
                    )
                    .language(
                            language
                    )
                    .speaker(
                            speaker
                    )
                    .provider(
                            PROVIDER_CODE
                    )
                    .model(
                            ttsProperties.getModel()
                    )
                    .finalResponse(
                            request.isFinalResponse()
                    )
                    .latencyMs(
                            latencyMs
                    )
                    .inputCharacters(
                            request.getText() != null
                                    ? request.getText()
                                    .length()
                                    : 0
                    )
                    .providerRequestId(
                            null
                    )
                    .build();

        } catch (Exception exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "Sarvam TTS synthesis failed. callId={}, provider={}, language={}, speaker={}, latencyMs={}",
                    request.getCallId(),
                    PROVIDER_CODE,
                    request.getLanguage(),
                    request.getSpeaker(),
                    latencyMs,
                    exception
            );

            throw new IllegalStateException(
                    TtsMessages
                            .SARVAM_SYNTHESIS_FAILED,
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {

        boolean available =
                ttsProperties.getEndpoint() != null
                        && !ttsProperties
                        .getEndpoint()
                        .isBlank()
                        && ttsProperties.getApiKey() != null
                        && !ttsProperties
                        .getApiKey()
                        .isBlank()
                        && ttsProperties.getApiKeyHeader() != null
                        && !ttsProperties
                        .getApiKeyHeader()
                        .isBlank();

        if (!available) {

            log.warn(
                    "Sarvam TTS provider is not fully configured."
            );
        }

        return available;
    }

    /**
     * Combines streamed audio chunks into a single byte array.
     *
     * @param chunks streamed audio chunks
     * @return combined audio
     */
    private byte[] combineAudioChunks(
            java.util.List<byte[]> chunks) {

        int totalLength = 0;

        for (byte[] chunk : chunks) {

            if (chunk != null) {
                totalLength += chunk.length;
            }
        }

        byte[] combinedAudio =
                new byte[totalLength];

        int currentPosition = 0;

        for (byte[] chunk : chunks) {

            if (chunk == null) {
                continue;
            }

            System.arraycopy(
                    chunk,
                    0,
                    combinedAudio,
                    currentPosition,
                    chunk.length
            );

            currentPosition +=
                    chunk.length;
        }

        return combinedAudio;
    }

    /**
     * Validates the generated audio size.
     *
     * @param audioBytes generated audio
     * @param callId call identifier
     */
    private void validateAudioSize(
            byte[] audioBytes,
            String callId) {

        Long maxAudioSizeBytes =
                ttsProperties
                        .getMaxAudioSizeBytes();

        if (maxAudioSizeBytes == null) {

            return;
        }

        if (audioBytes.length
                > maxAudioSizeBytes) {

            log.error(
                    "Generated TTS audio exceeds configured limit. callId={}, audioSizeBytes={}, maxAudioSizeBytes={}",
                    callId,
                    audioBytes.length,
                    maxAudioSizeBytes
            );

            throw new IllegalStateException(
                    TtsMessages
                            .SYNTHESIS_FAILED
            );
        }
    }

    /**
     * Returns the content type corresponding to the configured
     * streaming output codec.
     *
     * @return audio content type
     */
    private String resolveContentType() {

        if ("mp3".equalsIgnoreCase(
                OUTPUT_AUDIO_CODEC)) {

            return "audio/mpeg";
        }

        if ("wav".equalsIgnoreCase(
                OUTPUT_AUDIO_CODEC)) {

            return "audio/wav";
        }

        return MediaType
                .APPLICATION_OCTET_STREAM_VALUE;
    }

    /**
     * Request payload sent to Sarvam.
     *
     * <p>
     * Jackson converts the Java property names to the exact
     * snake-case fields expected by Sarvam.
     * </p>
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SarvamTtsRequest {

        private String text;

        @com.fasterxml.jackson.annotation.JsonProperty(
                "language_code"
        )
        private String languageCode;

        private String speaker;

        private String model;

        private Double pace;

        @com.fasterxml.jackson.annotation.JsonProperty(
                "speech_sample_rate"
        )
        private Integer speechSampleRate;

        @com.fasterxml.jackson.annotation.JsonProperty(
                "output_audio_codec"
        )
        private String outputAudioCodec;
    }
}