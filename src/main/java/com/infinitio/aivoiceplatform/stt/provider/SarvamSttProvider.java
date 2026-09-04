package com.infinitio.aivoiceplatform.stt.provider;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
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
     *
     * <p>
     * This identifies the provider implementation and is not
     * an environment-specific runtime configuration value.
     * </p>
     */
    private static final String PROVIDER_CODE =
            "sarvam";

    /**
     * Sarvam API authentication header.
     *
     * <p>
     * This is a provider protocol header name. The actual
     * API key is always loaded from external configuration.
     * </p>
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
                        "streamingModel={}, timeout={}",
                PROVIDER_CODE,
                sttProperties.getModel(),
                sttProperties.getMode(),
                sttProperties.getStreamingModel(),
                sttProperties.getTimeout()
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
                                    sttProperties.getApiKey()
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

        String streamingUri =
                buildStreamingUri(
                        sttProperties.getStreamingEndpoint(),
                        model,
                        language.trim(),
                        sampleRate,
                        resolvedEncoding
                );

        log.info(
                "Opening Sarvam realtime STT session. " +
                        "callId={}, model={}, language={}, " +
                        "sampleRate={}, encoding={}",
                callId,
                model,
                language,
                sampleRate,
                resolvedEncoding
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

            httpClient
                    .newWebSocketBuilder()
                    .header(
                            API_KEY_HEADER,
                            sttProperties.getApiKey()
                    )
                    .buildAsync(
                            URI.create(
                                    streamingUri
                            ),
                            streamingSession
                    )
                    .join();

            log.info(
                    "Sarvam realtime STT session established. " +
                            "callId={}",
                    callId
            );

            return streamingSession;

        } catch (Exception exception) {

            try {

                streamingSession.close();

            } catch (Exception closeException) {

                log.warn(
                        "Unable to close failed Sarvam STT " +
                                "streaming session. callId={}",
                        callId,
                        closeException
                );
            }

            log.error(
                    "Unable to establish Sarvam realtime STT session. " +
                            "callId={}",
                    callId,
                    exception
            );

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

        if (isBlank(
                sttProperties.getApiKey()
        )) {

            log.error(
                    "Sarvam STT API key is not configured."
            );

            throw new IllegalStateException(
                    SttMessages.PROVIDER_NOT_CONFIGURED
            );
        }
    }

    /**
     * Validates streaming request values.
     *
     * @param callId call identifier
     * @param language language
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
     * <p>
     * A model supplied by the runtime request takes precedence.
     * When the runtime request does not contain a model, the
     * configured default STT model is used.
     * </p>
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
    private String resolveStreamingEncoding(
            String audioEncoding) {

        if (isBlank(audioEncoding)) {

            throw new IllegalArgumentException(
                    SttMessages.STREAMING_AUDIO_ENCODING_REQUIRED
            );
        }

        return audioEncoding.trim();
    }

    /**
     * Builds the synchronous multipart request.
     *
     * @param request transcription request
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

        return uri.toString();
    }

    /**
     * Appends a query parameter when a value is configured.
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
                        encode(value)
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
                        "available={}",
                available
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
}