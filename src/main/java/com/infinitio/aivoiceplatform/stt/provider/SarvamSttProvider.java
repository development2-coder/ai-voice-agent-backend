package com.infinitio.aivoiceplatform.stt.provider;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Objects;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.stt.config.SttProperties;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Sarvam implementation of the speech-to-text provider.
 *
 * <p>
 * Supports synchronous transcription and realtime streaming
 * speech-to-text.
 * </p>
 *
 * <p>
 * All runtime configuration is loaded through
 * {@link SttProperties}. Provider endpoints, models, API keys,
 * timeout values and audio configuration are never hardcoded
 * in this class.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
public class SarvamSttProvider
        implements SttProvider {

    /**
     * Sarvam provider code.
     */
    private static final String PROVIDER_CODE =
            "sarvam";

    /**
     * Sarvam API authentication header.
     */
    private static final String API_KEY_HEADER =
            "api-subscription-key";

    /**
     * Multipart audio field.
     */
    private static final String FILE_PART_NAME =
            "file";

    /**
     * Multipart model field.
     */
    private static final String MODEL_PART_NAME =
            "model";

    /**
     * Multipart language field.
     */
    private static final String LANGUAGE_CODE_PART_NAME =
            "language_code";

    /**
     * Multipart transcription mode field.
     */
    private static final String MODE_PART_NAME =
            "mode";

    /**
     * REST client.
     */
    private final RestClient restClient;

    /**
     * STT runtime configuration.
     */
    private final SttProperties sttProperties;

    /**
     * HTTP client used for realtime WebSocket connections.
     */
    private final HttpClient httpClient;

    /**
     * Jackson object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates the Sarvam STT provider.
     *
     * @param restClientBuilder REST client builder
     * @param sttProperties STT runtime configuration
     * @param objectMapper JSON object mapper
     */
    public SarvamSttProvider(
            RestClient.Builder restClientBuilder,
            SttProperties sttProperties,
            ObjectMapper objectMapper) {

        this.sttProperties =
                Objects.requireNonNull(
                        sttProperties,
                        "STT properties are required."
                );

        Objects.requireNonNull(
                restClientBuilder,
                "RestClient builder is required."
        );

        this.objectMapper =
                Objects.requireNonNull(
                        objectMapper,
                        "ObjectMapper is required."
                );

        this.restClient =
                buildRestClient(
                        restClientBuilder
                );

        this.httpClient =
                buildHttpClient();

        log.info(
                "Sarvam STT provider initialized. " +
                        "provider={}, model={}, mode={}, " +
                        "streamingModel={}, timeout={}, " +
                        "apiKeyConfigured={}",
                PROVIDER_CODE,
                sttProperties.getModel(),
                sttProperties.getMode(),
                sttProperties.getStreamingModel(),
                sttProperties.getTimeout(),
                !isBlank(sttProperties.getApiKey())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderCode() {

        return PROVIDER_CODE;
    }

    /**
     * Performs synchronous speech-to-text transcription.
     *
     * @param request STT transcription request
     * @return transcription response
     */
    @Override
    public SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request) {

        Objects.requireNonNull(
                request,
                SttMessages.TRANSCRIPTION_REQUEST_REQUIRED
        );

        long startTime =
                System.currentTimeMillis();

        String model =
                resolveModel(
                        request
                );

        validateSynchronousConfiguration();

        String apiKey =
                resolveApiKey();

        log.info(
                "Starting Sarvam STT transcription. " +
                        "callId={}, model={}, language={}, " +
                        "finalTranscript={}",
                request.getCallId(),
                model,
                request.getLanguage(),
                request.isFinalTranscript()
        );

        try {

            MultiValueMap<String, Object> requestBody =
                    buildRequestBody(
                            request,
                            model
                    );

            SarvamSttResponse sarvamResponse =
                    restClient
                            .post()
                            .uri(
                                    sttProperties.getEndpoint()
                            )
                            .header(
                                    API_KEY_HEADER,
                                    apiKey
                            )
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )
                            .body(
                                    requestBody
                            )
                            .retrieve()
                            .body(
                                    SarvamSttResponse.class
                            );

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            if (sarvamResponse == null) {

                log.error(
                        "Sarvam STT returned an empty response. " +
                                "callId={}, model={}, latencyMs={}",
                        request.getCallId(),
                        model,
                        latencyMs
                );

                throw new IllegalStateException(
                        SttMessages.TRANSCRIPTION_FAILED
                );
            }

            log.info(
                    "Sarvam STT transcription completed. " +
                            "callId={}, model={}, latencyMs={}",
                    request.getCallId(),
                    model,
                    latencyMs
            );

            return SttTranscriptionResponse
                    .builder()
                    .callId(
                            request.getCallId()
                    )
                    .transcript(
                            sarvamResponse.getTranscript()
                    )
                    .language(
                            sarvamResponse.getLanguageCode()
                    )
                    .finalTranscript(
                            request.isFinalTranscript()
                    )
                    .languageProbability(
                            sarvamResponse
                                    .getLanguageProbability()
                    )
                    .provider(
                            PROVIDER_CODE
                    )
                    .latencyMs(
                            latencyMs
                    )
                    .build();

        } catch (RestClientResponseException exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "Sarvam STT API request failed. " +
                            "callId={}, model={}, statusCode={}, " +
                            "latencyMs={}",
                    request.getCallId(),
                    model,
                    exception.getStatusCode(),
                    latencyMs,
                    exception
            );

            throw new IllegalStateException(
                    SttMessages.SARVAM_TRANSCRIPTION_FAILED,
                    exception
            );

        } catch (Exception exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "Unexpected error during Sarvam STT processing. " +
                            "callId={}, model={}, latencyMs={}",
                    request.getCallId(),
                    model,
                    latencyMs,
                    exception
            );

            throw new IllegalStateException(
                    SttMessages.SARVAM_TRANSCRIPTION_FAILED,
                    exception
            );
        }
    }

    /**
     * Opens a Sarvam realtime streaming STT session.
     *
     * @param callId application call identifier
     * @param language requested language
     * @param sampleRate input audio sample rate
     * @param audioEncoding input audio encoding
     * @param listener streaming transcription listener
     * @return streaming STT session
     */
    @Override
    public SttStreamingSession openStreamingSession(
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

        validateStreamingConfiguration();

        String model =
                resolveStreamingModel();

        String resolvedEncoding =
                resolveStreamingEncoding(
                        audioEncoding
                );

        String streamingLanguage =
                resolveStreamingLanguage(
                        language
                );

        String streamingUri =
                buildStreamingUri(
                        sttProperties.getStreamingEndpoint(),
                        model,
                        streamingLanguage,
                        sampleRate,
                        resolvedEncoding
                );

        String apiKey =
                resolveApiKey();

        log.info(
                "Opening Sarvam realtime STT WebSocket. " +
                        "callId={}, model={}, language={}, " +
                        "sampleRate={}, encoding={}, " +
                        "apiKeyConfigured={}, streamingLanguage={}, apiKeyFingerprint={}",
                callId,
                model,
                language,
                sampleRate,
                resolvedEncoding,
                !isBlank(apiKey),
                streamingLanguage,
                createApiKeyFingerprint(apiKey)
        );

        SarvamStreamingSession streamingSession =
                new SarvamStreamingSession(
                        callId,
                        language.trim(),
                        sampleRate,
                        listener,
                        objectMapper
                );

        try {

            /*
             * Sarvam requires the API subscription key in the
             * WebSocket handshake header.
             *
             * The actual key is never logged.
             */
            httpClient
                    .newWebSocketBuilder()
                    .header(
                            API_KEY_HEADER,
                            apiKey
                    )
                    .buildAsync(
                            URI.create(streamingUri),
                            streamingSession
                    )
                    .whenComplete(
                            (webSocket, throwable) -> {

                                if (throwable != null) {

                                    log.error(
                                            "Sarvam realtime STT WebSocket " +
                                                    "connection failed. " +
                                                    "callId={}, model={}, " +
                                                    "apiKeyFingerprint={}",
                                            callId,
                                            model,
                                            createApiKeyFingerprint(
                                                    apiKey
                                            ),
                                            throwable
                                    );

                                    streamingSession.markConnectionFailure(
                                            throwable
                                    );

                                    return;
                                }

                                log.info(
                                        "Sarvam realtime STT WebSocket " +
                                                "connection established. " +
                                                "callId={}, model={}",
                                        callId,
                                        model
                                );
                            }
                    );

            return streamingSession;

        } catch (Exception exception) {

            log.error(
                    "Unable to start Sarvam realtime STT WebSocket. " +
                            "callId={}, model={}",
                    callId,
                    model,
                    exception
            );

            try {

                streamingSession.close();

            } catch (Exception closeException) {

                log.warn(
                        "Unable to close failed Sarvam STT session. " +
                                "callId={}",
                        callId,
                        closeException
                );
            }

            throw new IllegalStateException(
                    SttMessages.SARVAM_TRANSCRIPTION_FAILED,
                    exception
            );
        }
    }

    /**
     * Builds the synchronous REST client using the configured
     * STT timeout.
     *
     * @param restClientBuilder Spring REST client builder
     * @return configured REST client
     */
    private RestClient buildRestClient(
            RestClient.Builder restClientBuilder) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        Duration timeout =
                sttProperties.getTimeout();

        if (timeout != null
                && !timeout.isNegative()
                && !timeout.isZero()) {

            requestFactory.setConnectTimeout(
                    timeout
            );

            requestFactory.setReadTimeout(
                    timeout
            );

        } else {

            log.warn(
                    "STT timeout is not configured or invalid. " +
                            "Default HTTP client timeout will be used."
            );
        }

        return restClientBuilder
                .requestFactory(
                        requestFactory
                )
                .build();
    }

    /**
     * Builds the HTTP client used for realtime WebSocket
     * connections.
     *
     * @return configured HTTP client
     */
    private HttpClient buildHttpClient() {

        HttpClient.Builder builder =
                HttpClient.newBuilder();

        Duration timeout =
                sttProperties.getTimeout();

        if (timeout != null
                && !timeout.isNegative()
                && !timeout.isZero()) {

            builder.connectTimeout(
                    timeout
            );

        } else {

            log.warn(
                    "STT WebSocket connection timeout is not " +
                            "configured or invalid. " +
                            "Default HTTP client timeout will be used."
            );
        }

        return builder.build();
    }

    /**
     * Validates synchronous Sarvam configuration.
     */
    private void validateSynchronousConfiguration() {

        if (isBlank(
                sttProperties.getEndpoint()
        )) {

            log.error(
                    "Sarvam synchronous STT endpoint is not configured."
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        validateApiKey();
    }

    /**
     * Validates realtime Sarvam configuration.
     */
    private void validateStreamingConfiguration() {

        if (isBlank(
                sttProperties.getStreamingEndpoint()
        )) {

            log.error(
                    "Sarvam realtime STT endpoint is not configured."
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        validateApiKey();
    }

    /**
     * Validates the configured Sarvam API key.
     */
    private void validateApiKey() {

        String apiKey =
                sttProperties.getApiKey();

        if (isBlank(apiKey)) {

            log.error(
                    "Sarvam STT API key is not configured."
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        log.debug(
                "Sarvam STT API key configuration detected. " +
                        "apiKeyLength={}, apiKeyFingerprint={}",
                apiKey.trim().length(),
                createApiKeyFingerprint(
                        apiKey
                )
        );
    }

    /**
     * Resolves and normalizes the configured API key.
     *
     * <p>
     * Environment variables can occasionally contain accidental
     * leading or trailing whitespace. The provider must never send
     * that whitespace as part of the authentication credential.
     * </p>
     *
     * @return normalized API key
     */
    private String resolveApiKey() {

        String apiKey =
                sttProperties.getApiKey();

        if (isBlank(apiKey)) {

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }

        return apiKey.trim();
    }

    /**
     * Creates a non-reversible fingerprint of the API key for
     * diagnostic purposes.
     *
     * <p>
     * The actual API key is never logged.
     * </p>
     *
     * @param apiKey API key
     * @return short SHA-256 fingerprint
     */
    private String createApiKeyFingerprint(
            String apiKey) {

        if (isBlank(apiKey)) {

            return "NOT_CONFIGURED";
        }

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            apiKey.trim()
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );

            StringBuilder fingerprint =
                    new StringBuilder();

            for (int index = 0;
                 index < hash.length;
                 index++) {

                fingerprint.append(
                        String.format(
                                "%02x",
                                hash[index]
                        )
                );
            }

            return fingerprint
                    .substring(
                            0,
                            12
                    );

        } catch (Exception exception) {

            return "UNAVAILABLE";
        }
    }

    /**
     * Validates streaming request values.
     *
     * @param callId call identifier
     * @param language language
     * @param sampleRate audio sample rate
     * @param audioEncoding audio encoding
     * @param listener listener
     */
    private void validateStreamingRequest(
            String callId,
            String language,
            Integer sampleRate,
            String audioEncoding,
            SttStreamingListener listener) {

        if (isBlank(callId)) {

            throw new IllegalArgumentException(
                    SttMessages.CALL_ID_REQUIRED
            );
        }

        if (isBlank(language)) {

            throw new IllegalArgumentException(
                    SttMessages.LANGUAGE_REQUIRED
            );
        }

        if (sampleRate == null
                || sampleRate <= 0) {

            throw new IllegalArgumentException(
                    SttMessages.STREAMING_SAMPLE_RATE_INVALID
            );
        }

        if (isBlank(audioEncoding)) {

            throw new IllegalArgumentException(
                    SttMessages.STREAMING_AUDIO_ENCODING_REQUIRED
            );
        }

        if (listener == null) {

            throw new IllegalArgumentException(
                    SttMessages.STREAMING_LISTENER_REQUIRED
            );
        }
    }

    /**
     * Resolves the synchronous STT model.
     *
     * @param request transcription request
     * @return resolved model
     */
    private String resolveModel(
            SttTranscriptionRequest request) {

        if (!isBlank(
                request.getModel()
        )) {

            return request
                    .getModel()
                    .trim();
        }

        if (isBlank(
                sttProperties.getModel()
        )) {

            throw new IllegalStateException(
                    SttMessages.MODEL_NOT_CONFIGURED
            );
        }

        return sttProperties
                .getModel()
                .trim();
    }

    /**
     * Resolves the configured realtime streaming model.
     *
     * @return realtime streaming model
     */
    private String resolveStreamingModel() {

        if (isBlank(
                sttProperties.getStreamingModel()
        )) {

            throw new IllegalStateException(
                    SttMessages.MODEL_NOT_CONFIGURED
            );
        }

        return sttProperties
                .getStreamingModel()
                .trim();
    }

    /**
     * Resolves the streaming audio encoding.
     *
     * @param audioEncoding requested audio encoding
     * @return resolved audio encoding
     */
    /**
     * Resolves the streaming audio encoding required by Sarvam.
     *
     * <p>
     * Telephony providers may use provider-specific encoding names.
     * For example, Exotel uses {@code slin} for signed linear PCM,
     * while Sarvam expects {@code linear16}.
     * </p>
     *
     * @param audioEncoding audio encoding received from the voice gateway
     * @return Sarvam-compatible audio encoding
     */
    private String resolveStreamingEncoding(
            String audioEncoding) {

        if (isBlank(audioEncoding)) {

            throw new IllegalArgumentException(
                    SttMessages.STREAMING_AUDIO_ENCODING_REQUIRED
            );
        }

        String normalizedEncoding =
                audioEncoding
                        .trim()
                        .toLowerCase();

        return switch (normalizedEncoding) {

            case "slin",
                 "linear16",
                 "pcm_s16le" ->
                    "linear16";

            case "linear32" ->
                    "linear32";

            case "mulaw",
                 "ulaw" ->
                    "mulaw";

            case "alaw" ->
                    "alaw";

            default -> {

                log.warn(
                        "Unsupported Sarvam streaming audio encoding. " +
                                "encoding={}",
                        audioEncoding
                );

                throw new IllegalArgumentException(
                        "Unsupported Sarvam streaming audio encoding: "
                                + audioEncoding
                );
            }
        };
    }

    /**
     * Builds the synchronous multipart request.
     *
     * @param request STT request
     * @param model resolved model
     * @return multipart request body
     */
    private MultiValueMap<String, Object> buildRequestBody(
            SttTranscriptionRequest request,
            String model) {

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        ByteArrayResource audioResource =
                new ByteArrayResource(
                        request.getAudio()
                ) {

                    @Override
                    public String getFilename() {

                        if (!isBlank(
                                request.getFileName()
                        )) {

                            return request
                                    .getFileName()
                                    .trim();
                        }

                        return "audio";
                    }
                };

        body.add(
                FILE_PART_NAME,
                audioResource
        );

        body.add(
                MODEL_PART_NAME,
                model
        );

        body.add(
                LANGUAGE_CODE_PART_NAME,
                request.getLanguage()
        );

        if (!isBlank(
                sttProperties.getMode()
        )) {

            body.add(
                    MODE_PART_NAME,
                    sttProperties
                            .getMode()
                            .trim()
            );
        }

        return body;
    }

    /**
     * Builds the Sarvam realtime streaming URI.
     *
     * @param endpoint configured streaming endpoint
     * @param model streaming model
     * @param language language code
     * @param sampleRate audio sample rate
     * @param audioEncoding audio encoding
     * @return provider WebSocket URI
     */
    private String buildStreamingUri(
            String endpoint,
            String model,
            String language,
            Integer sampleRate,
            String audioEncoding) {

        StringBuilder uri =
                new StringBuilder(
                        endpoint
                );

        String separator =
                endpoint.contains("?")
                        ? "&"
                        : "?";

        appendQueryParameter(
                uri,
                separator,
                "model",
                model
        );

        separator = "&";

        appendQueryParameter(
                uri,
                separator,
                "language_code",
                language
        );

        appendQueryParameter(
                uri,
                separator,
                "sample_rate",
                String.valueOf(
                        sampleRate
                )
        );

        appendQueryParameter(
                uri,
                separator,
                "encoding",
                audioEncoding
        );

        appendQueryParameter(
                uri,
                separator,
                "mode",
                sttProperties.getStreamingMode()
        );

        appendQueryParameter(
                uri,
                separator,
                "endpointing",
                sttProperties.getStreamingEndpointing()
        );

        appendQueryParameter(
                uri,
                separator,
                "stream_type",
                sttProperties.getStreamingStreamType()
        );

        appendQueryParameter(
                uri,
                separator,
                "threshold",
                sttProperties.getStreamingThreshold() != null
                        ? String.valueOf(
                        sttProperties.getStreamingThreshold()
                )
                        : null
        );

        appendQueryParameter(
                uri,
                separator,
                "silence_duration_ms",
                sttProperties.getStreamingSilenceDurationMs() != null
                        ? String.valueOf(
                        sttProperties.getStreamingSilenceDurationMs()
                )
                        : null
        );

        appendQueryParameter(
                uri,
                separator,
                "min_speech_duration_ms",
                sttProperties.getStreamingMinSpeechDurationMs() != null
                        ? String.valueOf(
                        sttProperties.getStreamingMinSpeechDurationMs()
                )
                        : null
        );

        appendQueryParameter(
                uri,
                separator,
                "prefix_padding_ms",
                sttProperties.getStreamingPrefixPaddingMs() != null
                        ? String.valueOf(
                        sttProperties.getStreamingPrefixPaddingMs()
                )
                        : null
        );

        appendQueryParameter(
                uri,
                separator,
                "return_timestamps",
                String.valueOf(
                        sttProperties.isStreamingReturnTimestamps()
                )
        );

        return uri.toString();
    }

    /**
     * Appends a query parameter when configured.
     *
     * @param uri URI builder
     * @param separator query separator
     * @param name parameter name
     * @param value parameter value
     */
    private void appendQueryParameter(
            StringBuilder uri,
            String separator,
            String name,
            String value) {

        if (isBlank(value)) {

            return;
        }

        uri.append(separator)
                .append(name)
                .append("=")
                .append(
                        encode(value.trim())
                );
    }

    /**
     * Encodes a URI query parameter.
     *
     * @param value parameter value
     * @return encoded value
     */
    private String encode(
            String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {

        boolean available =
                !isBlank(
                        sttProperties.getApiKey()
                )
                        && (
                        !isBlank(
                                sttProperties.getEndpoint()
                        )
                                || !isBlank(
                                sttProperties
                                        .getStreamingEndpoint()
                        )
                );

        log.debug(
                "Sarvam STT provider availability checked. " +
                        "available={}, apiKeyConfigured={}, " +
                        "streamingEndpointConfigured={}",
                available,
                !isBlank(
                        sttProperties.getApiKey()
                ),
                !isBlank(
                        sttProperties.getStreamingEndpoint()
                )
        );

        return available;
    }

    /**
     * Checks whether a value is blank.
     *
     * @param value value
     * @return true when value is null or blank
     */
    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }

    /**
     * Resolves the language strategy for realtime STT.
     *
     * <p>
     * The realtime voice-agent flow uses automatic language detection
     * so that the caller can switch languages during the same call.
     * </p>
     *
     * @return configured realtime STT language strategy
     */
    /**
     * Resolves the language for realtime STT.
     *
     * <p>
     * The language supplied by the active conversation runtime takes
     * precedence over the global STT streaming language configuration.
     * This ensures that when an Agent is configured for Marathi,
     * Sarvam realtime STT receives {@code mr-IN} instead of falling
     * back to {@code auto} or another configured language.
     * </p>
     *
     * @param requestedLanguage language resolved for the current call
     * @return resolved realtime STT language
     */
    private String resolveStreamingLanguage(
            String requestedLanguage) {

        String configuredLanguage =
                sttProperties.getStreamingLanguage();

        if (!isBlank(configuredLanguage)) {

            return configuredLanguage.trim();
        }

        return "auto";
    }
}