package com.infinitio.aivoiceplatform.tts.provider;

import java.util.Base64;
import java.util.List;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infinitio.aivoiceplatform.tts.config.TtsProperties;
import com.infinitio.aivoiceplatform.tts.constant.TtsMessages;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.streaming.TtsAudioStreamListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Implements Sarvam text-to-speech provider integration.
 *
 * <p>
 * Provider endpoint, authentication, model, speaker, sample rate,
 * output codec and other runtime values are loaded from external
 * configuration through {@link TtsProperties}.
 * </p>
 *
 * <p>
 * The provider supports both synchronous and streaming TTS execution.
 * During streaming execution, audio chunks received from Sarvam are
 * immediately forwarded to the supplied listener while the complete
 * audio is also collected for the existing storage and persistence flow.
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
    private static final String PROVIDER_CODE = "sarvam";

    /**
     * WebClient used for Sarvam requests.
     */
    private final WebClient sarvamWebClient;

    /**
     * Runtime TTS configuration.
     */
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

        return synthesizeInternal(
                request,
                null
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Audio chunks are forwarded immediately to the supplied
     * listener while the complete response is collected for
     * the existing TTS storage and persistence flow.
     * </p>
     */
    @Override
    public TtsSynthesisResponse synthesizeStreaming(
            TtsSynthesisRequest request,
            TtsAudioStreamListener listener) {

        return synthesizeInternal(
                request,
                listener
        );
    }

    /**
     * Executes synchronous or streaming Sarvam TTS synthesis.
     *
     * @param request TTS synthesis request
     * @param listener optional audio stream listener
     * @return TTS synthesis response
     */
    private TtsSynthesisResponse synthesizeInternal(
            TtsSynthesisRequest request,
            TtsAudioStreamListener listener) {

        long startTime =
                System.currentTimeMillis();

        validateRequest(request);

        String language =
                request.getLanguage().trim();

        String speaker =
                request.getSpeaker() != null
                        && !request.getSpeaker().isBlank()
                        ? request.getSpeaker().trim()
                        : resolveDefaultSpeaker();

        Double pace =
                request.getPace() != null
                        ? request.getPace()
                        : ttsProperties.getDefaultPace();

        Integer speechSampleRate =
                request.getSpeechSampleRate() != null
                        ? request.getSpeechSampleRate()
                        : ttsProperties.getDefaultSpeechSampleRate();

        String outputAudioCodec =
                resolveOutputAudioCodec();

        log.info(
                "Starting Sarvam TTS synthesis. " +
                        "callId={}, language={}, speaker={}, " +
                        "textLength={}, finalResponse={}, streaming={}",
                request.getCallId(),
                language,
                speaker,
                request.getText() != null
                        ? request.getText().length()
                        : 0,
                request.isFinalResponse(),
                listener != null
        );

        try {

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
                                    resolveModel()
                            )
                            .pace(
                                    pace
                            )
                            .speechSampleRate(
                                    speechSampleRate
                            )
                            .outputAudioCodec(
                                    outputAudioCodec
                            )
                            .build();

            log.info(
                    "Sending TTS request to Sarvam. " +
                            "callId={}, provider={}, model={}, " +
                            "language={}, speaker={}, pace={}, " +
                            "sampleRate={}, codec={}, streaming={}",
                    request.getCallId(),
                    PROVIDER_CODE,
                    sarvamRequest.getModel(),
                    language,
                    speaker,
                    pace,
                    speechSampleRate,
                    outputAudioCodec,
                    listener != null
            );

            Flux<DataBuffer> audioFlux =
                    sarvamWebClient
                            .post()
                            .uri(
                                    resolveStreamPath()
                            )
                            .header(
                                    HttpHeaders.CONTENT_TYPE,
                                    MediaType.APPLICATION_JSON_VALUE
                            )
                            .header(
                                    resolveApiKeyHeader(),
                                    resolveApiKey()
                            )
                            .bodyValue(
                                    sarvamRequest
                            )
                            .retrieve()
                            .bodyToFlux(
                                    DataBuffer.class
                            );

            /*
             * Sarvam sends audio as a stream of DataBuffer chunks.
             *
             * Each chunk is copied into a byte array and immediately
             * forwarded to the listener when streaming is enabled.
             *
             * The same chunks are also collected so that the complete
             * audio can continue through the existing storage flow.
             */
            List<byte[]> audioChunks =
                    audioFlux
                            .map(
                                    dataBuffer -> {

                                        try {

                                            byte[] bytes =
                                                    new byte[
                                                            dataBuffer
                                                                    .readableByteCount()
                                                            ];

                                            dataBuffer.read(
                                                    bytes
                                            );

                                            return bytes;

                                        } finally {

                                            DataBufferUtils.release(
                                                    dataBuffer
                                            );
                                        }
                                    }
                            )
                            .doOnNext(
                                    audioChunk -> {

                                        if (listener != null
                                                && audioChunk != null
                                                && audioChunk.length > 0) {

                                            listener.onAudioChunk(
                                                    audioChunk,
                                                    resolveContentType(
                                                            outputAudioCodec
                                                    )
                                            );

                                            log.debug(
                                                    "Forwarded Sarvam TTS audio chunk. " +
                                                            "callId={}, chunkSizeBytes={}",
                                                    request.getCallId(),
                                                    audioChunk.length
                                            );
                                        }
                                    }
                            )
                            .collectList()
                            .block();

            byte[] audioBytes =
                    combineAudioChunks(
                            audioChunks
                    );

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            if (audioBytes == null
                    || audioBytes.length == 0) {

                log.error(
                        "Sarvam TTS returned empty audio. " +
                                "callId={}, latencyMs={}",
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
                    "Sarvam TTS synthesis completed successfully. " +
                            "callId={}, provider={}, model={}, " +
                            "language={}, speaker={}, audioSizeBytes={}, " +
                            "latencyMs={}, streaming={}",
                    request.getCallId(),
                    PROVIDER_CODE,
                    sarvamRequest.getModel(),
                    language,
                    speaker,
                    audioBytes.length,
                    latencyMs,
                    listener != null
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
                            resolveContentType(
                                    outputAudioCodec
                            )
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
                            sarvamRequest.getModel()
                    )
                    .finalResponse(
                            request.isFinalResponse()
                    )
                    .latencyMs(
                            latencyMs
                    )
                    .inputCharacters(
                            request.getText() != null
                                    ? request.getText().length()
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
                    "Sarvam TTS synthesis failed. " +
                            "callId={}, provider={}, language={}, " +
                            "speaker={}, latencyMs={}, streaming={}",
                    request.getCallId(),
                    PROVIDER_CODE,
                    language,
                    speaker,
                    latencyMs,
                    listener != null,
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
                isConfigured(
                        ttsProperties.getEndpoint()
                )
                        && isConfigured(
                        ttsProperties.getStreamPath()
                )
                        && isConfigured(
                        ttsProperties.getApiKey()
                )
                        && isConfigured(
                        ttsProperties.getApiKeyHeader()
                )
                        && isConfigured(
                        ttsProperties.getModel()
                )
                        && isConfigured(
                        ttsProperties.getOutputAudioCodec()
                );

        if (!available) {

            log.warn(
                    "Sarvam TTS provider is not fully configured."
            );
        }

        return available;
    }

    /**
     * Validates the incoming synthesis request.
     *
     * @param request synthesis request
     */
    private void validateRequest(
            TtsSynthesisRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    TtsMessages.SYNTHESIS_REQUEST_REQUIRED
            );
        }

        if (request.getText() == null
                || request.getText().isBlank()) {

            throw new IllegalArgumentException(
                    TtsMessages.TEXT_REQUIRED
            );
        }

        if (request.getLanguage() == null
                || request.getLanguage().isBlank()) {

            throw new IllegalArgumentException(
                    TtsMessages.LANGUAGE_REQUIRED
            );
        }
    }

    /**
     * Resolves the configured Sarvam stream path.
     *
     * @return provider stream path
     */
    private String resolveStreamPath() {

        String streamPath =
                ttsProperties.getStreamPath();

        if (!isConfigured(streamPath)) {

            throw new IllegalStateException(
                    "TTS streaming path is not configured."
            );
        }

        return streamPath.trim();
    }

    /**
     * Resolves the configured output audio codec.
     *
     * @return configured output audio codec
     */
    private String resolveOutputAudioCodec() {

        String outputAudioCodec =
                ttsProperties.getOutputAudioCodec();

        if (!isConfigured(outputAudioCodec)) {

            throw new IllegalStateException(
                    "TTS output audio codec is not configured."
            );
        }

        return outputAudioCodec.trim();
    }

    /**
     * Resolves the configured Sarvam model.
     *
     * @return configured model
     */
    private String resolveModel() {

        String model =
                ttsProperties.getModel();

        if (!isConfigured(model)) {

            throw new IllegalStateException(
                    TtsMessages.MODEL_NOT_CONFIGURED
            );
        }

        return model.trim();
    }

    /**
     * Resolves the configured API key.
     *
     * @return API key
     */
    private String resolveApiKey() {

        String apiKey =
                ttsProperties.getApiKey();

        if (!isConfigured(apiKey)) {

            throw new IllegalStateException(
                    TtsMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        return apiKey.trim();
    }

    /**
     * Resolves the configured API key header.
     *
     * @return API key header
     */
    private String resolveApiKeyHeader() {

        String apiKeyHeader =
                ttsProperties.getApiKeyHeader();

        if (!isConfigured(apiKeyHeader)) {

            throw new IllegalStateException(
                    TtsMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        return apiKeyHeader.trim();
    }

    /**
     * Resolves the configured default speaker.
     *
     * @return default speaker
     */
    private String resolveDefaultSpeaker() {

        String speaker =
                ttsProperties.getDefaultSpeaker();

        if (!isConfigured(speaker)) {

            throw new IllegalStateException(
                    "TTS default speaker is not configured."
            );
        }

        return speaker.trim();
    }

    /**
     * Checks whether a configuration value is present.
     *
     * @param value configuration value
     * @return {@code true} when configured
     */
    private boolean isConfigured(
            String value) {

        return value != null
                && !value.isBlank();
    }

    /**
     * Combines streamed audio chunks into a single byte array.
     *
     * @param chunks streamed audio chunks
     * @return combined audio
     */
    private byte[] combineAudioChunks(
            List<byte[]> chunks) {

        if (chunks == null
                || chunks.isEmpty()) {

            return new byte[0];
        }

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
     * Validates generated audio size.
     *
     * @param audioBytes generated audio
     * @param callId application call identifier
     */
    private void validateAudioSize(
            byte[] audioBytes,
            String callId) {

        Long maxAudioSizeBytes =
                ttsProperties.getMaxAudioSizeBytes();

        if (maxAudioSizeBytes == null) {

            return;
        }

        if (audioBytes.length
                > maxAudioSizeBytes) {

            log.error(
                    "Generated TTS audio exceeds configured limit. " +
                            "callId={}, audioSizeBytes={}, " +
                            "maxAudioSizeBytes={}",
                    callId,
                    audioBytes.length,
                    maxAudioSizeBytes
            );

            throw new IllegalStateException(
                    TtsMessages.SYNTHESIS_FAILED
            );
        }
    }

    /**
     * Resolves the response content type from the configured codec.
     *
     * @param outputAudioCodec configured output codec
     * @return audio content type
     */
    private String resolveContentType(
            String outputAudioCodec) {

        if ("mp3".equalsIgnoreCase(
                outputAudioCodec)) {

            return "audio/mpeg";
        }

        if ("wav".equalsIgnoreCase(
                outputAudioCodec)) {

            return "audio/wav";
        }

        if ("linear16".equalsIgnoreCase(
                outputAudioCodec)) {

            return "audio/l16";
        }

        return MediaType
                .APPLICATION_OCTET_STREAM_VALUE;
    }

    /**
     * Request payload sent to Sarvam.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SarvamTtsRequest {

        /**
         * Text to synthesize.
         */
        private String text;

        /**
         * Target language.
         */
        @JsonProperty("language_code")
        private String languageCode;

        /**
         * Requested speaker.
         */
        private String speaker;

        /**
         * Sarvam TTS model.
         */
        private String model;

        /**
         * Speech pace.
         */
        private Double pace;

        /**
         * Speech sample rate.
         */
        @JsonProperty("speech_sample_rate")
        private Integer speechSampleRate;

        /**
         * Output audio codec.
         */
        @JsonProperty("output_audio_codec")
        private String outputAudioCodec;
    }
}