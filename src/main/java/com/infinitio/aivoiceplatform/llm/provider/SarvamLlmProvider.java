package com.infinitio.aivoiceplatform.llm.provider;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infinitio.aivoiceplatform.llm.config.LlmProperties;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmMessageDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides Sarvam LLM runtime integration.
 *
 * <p>
 * This implementation communicates with the Sarvam Chat Completions API
 * using the configured conversational model.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SarvamLlmProvider implements LlmProvider {

    private final WebClient webClient;

    private final LlmProperties llmProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderCode() {

        return "sarvam";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {

        boolean available =
                llmProperties.getEndpoint() != null
                        && !llmProperties.getEndpoint().isBlank()
                        && llmProperties.getApiKey() != null
                        && !llmProperties.getApiKey().isBlank()
                        && llmProperties.getModel() != null
                        && !llmProperties.getModel().isBlank();

        log.debug(
                "Sarvam LLM provider availability checked. available={}",
                available
        );

        return available;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LlmGenerationResponseDto generate(
            LlmGenerationRequestDto request) {

        long startTime =
                System.currentTimeMillis();

        log.info(
                "Starting Sarvam LLM generation. callId={}, language={}, messageCount={}, model={}",
                request.getCallId(),
                request.getLanguage(),
                request.getMessages() == null
                        ? 0
                        : request.getMessages().size(),
                llmProperties.getModel()
        );

        try {

            SarvamChatRequest sarvamRequest =
                    buildRequest(request);

            SarvamChatResponse sarvamResponse =
                    webClient
                            .post()
                            .uri(
                                    llmProperties
                                            .getEndpoint()
                            )
                            .header(
                                    resolveApiKeyHeader(),
                                    llmProperties
                                            .getApiKey()
                            )
                            .header(
                                    HttpHeaders.CONTENT_TYPE,
                                    MediaType.APPLICATION_JSON_VALUE
                            )
                            .bodyValue(sarvamRequest)
                            .retrieve()
                            .bodyToMono(
                                    SarvamChatResponse.class
                            )
                            .block(
                                    llmProperties
                                            .getTimeout()
                            );

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            validateResponse(
                    sarvamResponse
            );

            LlmGenerationResponseDto response =
                    buildResponse(
                            request,
                            sarvamResponse,
                            latencyMs
                    );

            log.info(
                    "Sarvam LLM generation completed. callId={}, model={}, latencyMs={}, inputTokens={}, outputTokens={}, totalTokens={}",
                    request.getCallId(),
                    response.getModel(),
                    response.getLatencyMs(),
                    response.getInputTokens(),
                    response.getOutputTokens(),
                    response.getTotalTokens()
            );

            return response;

        } catch (Exception exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "Sarvam LLM generation failed. callId={}, latencyMs={}",
                    request.getCallId(),
                    latencyMs,
                    exception
            );

            throw exception;
        }
    }

    /**
     * Builds the Sarvam API request.
     *
     * @param request runtime LLM request
     * @return Sarvam API request
     */
    private SarvamChatRequest buildRequest(
            LlmGenerationRequestDto request) {

        List<SarvamMessage> messages =
                request.getMessages()
                        .stream()
                        .map(
                                this::mapMessage
                        )
                        .toList();

        return SarvamChatRequest.builder()
                .model(
                        llmProperties
                                .getModel()
                )
                .messages(messages)
                .temperature(
                        llmProperties
                                .getTemperature()
                )
                .topP(
                        llmProperties
                                .getTopP()
                )
                .maxTokens(
                        llmProperties
                                .getMaxTokens()
                )
                .stream(
                        Boolean.TRUE.equals(
                                llmProperties
                                        .getStream()
                        )
                )
                .build();
    }

    /**
     * Maps an application message to the Sarvam message structure.
     *
     * @param message application message
     * @return Sarvam message
     */
    private SarvamMessage mapMessage(
            LlmMessageDto message) {

        log.debug(
                "Mapping LLM message for Sarvam request. role={}, contentLength={}",
                message.getRole(),
                message.getContent() == null
                        ? 0
                        : message.getContent().length()
        );

        return SarvamMessage.builder()
                .role(message.getRole())
                .content(message.getContent())
                .build();
    }

    /**
     * Builds the runtime response from the Sarvam response.
     *
     * @param request original runtime request
     * @param sarvamResponse Sarvam response
     * @param latencyMs provider latency
     * @return runtime response
     */
    private LlmGenerationResponseDto buildResponse(
            LlmGenerationRequestDto request,
            SarvamChatResponse sarvamResponse,
            long latencyMs) {

        SarvamChoice choice =
                sarvamResponse
                        .getChoices()
                        .get(0);

        String content =
                choice
                        .getMessage()
                        .getContent();

        SarvamUsage usage =
                sarvamResponse.getUsage();

        return LlmGenerationResponseDto.builder()
                .callId(request.getCallId())
                .content(content)
                .language(request.getLanguage())
                .provider(getProviderCode())
                .model(sarvamResponse.getModel())
                .finalResponse(
                        request.isFinalResponse()
                )
                .latencyMs(latencyMs)
                .inputTokens(
                        usage == null
                                ? null
                                : usage.getPromptTokens()
                )
                .outputTokens(
                        usage == null
                                ? null
                                : usage.getCompletionTokens()
                )
                .totalTokens(
                        usage == null
                                ? null
                                : usage.getTotalTokens()
                )
                .providerRequestId(
                        sarvamResponse.getId()
                )
                .build();
    }

    /**
     * Validates the Sarvam response.
     *
     * @param response Sarvam response
     */
    private void validateResponse(
            SarvamChatResponse response) {

        if (response == null) {

            throw new IllegalStateException(
                    "Sarvam LLM provider returned a null response."
            );
        }

        if (response.getChoices() == null
                || response.getChoices().isEmpty()) {

            throw new IllegalStateException(
                    "Sarvam LLM provider returned no choices."
            );
        }

        SarvamChoice choice =
                response.getChoices().get(0);

        if (choice == null
                || choice.getMessage() == null
                || choice.getMessage().getContent() == null
                || choice.getMessage().getContent().isBlank()) {

            throw new IllegalStateException(
                    "Sarvam LLM provider returned empty content."
            );
        }
    }

    /**
     * Resolves the configured API authentication header.
     *
     * @return API authentication header
     */
    private String resolveApiKeyHeader() {

        if (llmProperties.getApiKeyHeader() == null
                || llmProperties
                .getApiKeyHeader()
                .isBlank()) {

            return "api-subscription-key";
        }

        return llmProperties
                .getApiKeyHeader();
    }

    /**
     * Represents the Sarvam Chat Completions request.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SarvamChatRequest {

        private String model;

        private List<SarvamMessage> messages;

        private Double temperature;

        @JsonProperty("top_p")
        private Double topP;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        private Boolean stream;
    }

    /**
     * Represents a Sarvam conversation message.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SarvamMessage {

        private String role;

        private String content;
    }

    /**
     * Represents the Sarvam Chat Completions response.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SarvamChatResponse {

        private String id;

        private String model;

        private List<SarvamChoice> choices;

        private SarvamUsage usage;
    }

    /**
     * Represents a Sarvam response choice.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SarvamChoice {

        private SarvamResponseMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    /**
     * Represents the generated Sarvam message.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SarvamResponseMessage {

        private String role;

        private String content;
    }

    /**
     * Represents Sarvam token usage information.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SarvamUsage {

        @JsonProperty("prompt_tokens")
        private Long promptTokens;

        @JsonProperty("completion_tokens")
        private Long completionTokens;

        @JsonProperty("total_tokens")
        private Long totalTokens;
    }
}