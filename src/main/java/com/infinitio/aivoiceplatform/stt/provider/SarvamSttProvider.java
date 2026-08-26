package com.infinitio.aivoiceplatform.stt.provider;

import java.util.Objects;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infinitio.aivoiceplatform.stt.config.SttProperties;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Sarvam implementation of the speech-to-text provider.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
public class SarvamSttProvider implements SttProvider {

    private static final String PROVIDER_CODE = "sarvam";

    private static final String API_KEY_HEADER =
            "api-subscription-key";

    private static final String FILE_PART_NAME = "file";

    private static final String MODEL_PART_NAME = "model";

    private static final String LANGUAGE_CODE_PART_NAME =
            "language_code";

    private static final String MODE_PART_NAME = "mode";

    private final RestClient restClient;

    private final SttProperties sttProperties;

    /**
     * Creates the Sarvam STT provider.
     *
     * @param restClientBuilder REST client builder
     * @param sttProperties STT configuration
     */
    public SarvamSttProvider(
            RestClient.Builder restClientBuilder,
            SttProperties sttProperties) {

        this.sttProperties = sttProperties;

        this.restClient =
                restClientBuilder.build();

        log.info(
                "Sarvam STT provider initialized. model={}, mode={}",
                sttProperties.getModel(),
                sttProperties.getMode()
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
     * {@inheritDoc}
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

        log.info(
                "Starting Sarvam STT transcription. callId={}, language={}, finalTranscript={}",
                request.getCallId(),
                request.getLanguage(),
                request.isFinalTranscript()
        );

        try {

            MultiValueMap<String, Object> requestBody =
                    buildRequestBody(request);

            SarvamSttResponse sarvamResponse =
                    restClient.post()
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
                            .body(requestBody)
                            .retrieve()
                            .body(
                                    SarvamSttResponse.class
                            );

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            if (sarvamResponse == null) {

                log.error(
                        "Sarvam STT returned an empty response. callId={}, latencyMs={}",
                        request.getCallId(),
                        latencyMs
                );

                throw new IllegalStateException(
                        SttMessages.TRANSCRIPTION_FAILED
                );
            }

            log.info(
                    "Sarvam STT transcription completed. callId={}, latencyMs={}",
                    request.getCallId(),
                    latencyMs
            );

            return SttTranscriptionResponse
                    .builder()
                    .callId(request.getCallId())
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
                            sarvamResponse.getLanguageProbability()
                    )
                    .provider(
                            PROVIDER_CODE
                    )
                    .latencyMs(latencyMs)
                    .build();

        } catch (RestClientResponseException exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "Sarvam STT API request failed. callId={}, statusCode={}, latencyMs={}",
                    request.getCallId(),
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
                    "Unexpected error during Sarvam STT processing. callId={}, latencyMs={}",
                    request.getCallId(),
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
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {

        boolean available =
                sttProperties.getApiKey() != null
                        && !sttProperties
                        .getApiKey()
                        .isBlank()
                        && sttProperties.getEndpoint() != null
                        && !sttProperties
                        .getEndpoint()
                        .isBlank();

        log.debug(
                "Sarvam STT provider availability checked. available={}",
                available
        );

        return available;
    }

    /**
     * Builds the multipart request expected by Sarvam STT REST API.
     *
     * @param request STT transcription request
     * @return multipart request body
     */
    private MultiValueMap<String, Object> buildRequestBody(
            SttTranscriptionRequest request) {

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        ByteArrayResource audioResource =
                new ByteArrayResource(
                        request.getAudio()
                ) {

                    @Override
                    public String getFilename() {

                        return "audio";
                    }
                };

        body.add(
                FILE_PART_NAME,
                audioResource
        );

        body.add(
                MODEL_PART_NAME,
                sttProperties.getModel()
        );

        body.add(
                LANGUAGE_CODE_PART_NAME,
                request.getLanguage()
        );

        /*
         * Sarvam documents mode for the REST STT API.
         * It is omitted when no mode has been configured.
         */
        if (sttProperties.getMode() != null
                && !sttProperties
                .getMode()
                .isBlank()) {

            body.add(
                    MODE_PART_NAME,
                    sttProperties.getMode()
            );
        }

        return body;
    }

    /**
     * Represents the Sarvam STT REST response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SarvamSttResponse {

        @JsonProperty("request_id")
        private String requestId;

        private String transcript;

        @JsonProperty("language_code")
        private String languageCode;

        @JsonProperty("language_probability")
        private Double languageProbability;

        public String getRequestId() {

            return requestId;
        }

        public String getTranscript() {

            return transcript;
        }

        public String getLanguageCode() {

            return languageCode;
        }

        public Double getLanguageProbability() {

            return languageProbability;
        }
    }
}