package com.infinitio.aivoiceplatform.llm.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.llm.config.LlmProperties;
import com.infinitio.aivoiceplatform.llm.constant.LlmMessages;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmMessageDto;
import com.infinitio.aivoiceplatform.llm.provider.LlmProvider;
import com.infinitio.aivoiceplatform.llm.service.LlmRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements runtime LLM business operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmRuntimeServiceImpl
        implements LlmRuntimeService {

    private final LlmProvider llmProvider;

    private final LlmProperties llmProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public LlmGenerationResponseDto generate(
            LlmGenerationRequestDto request) {

        validateRequest(request);

        log.info(
                "Starting LLM generation. callId={}, language={}, messageCount={}, finalResponse={}",
                request.getCallId(),
                request.getLanguage(),
                request.getMessages().size(),
                request.isFinalResponse()
        );

        long startTime =
                System.currentTimeMillis();

        try {

            validateLanguage(
                    request.getLanguage()
            );

            validateMessages(
                    request.getMessages(),
                    request.getCallId()
            );

            validateProvider();

            LlmGenerationResponseDto response =
                    llmProvider.generate(request);

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            if (response == null) {

                log.error(
                        "LLM provider returned an empty response. callId={}, latencyMs={}",
                        request.getCallId(),
                        latencyMs
                );

                throw new IllegalStateException(
                        LlmMessages.EMPTY_PROVIDER_RESPONSE
                );
            }

            /*
             * Provider latency is retained when supplied by the provider.
             * Runtime latency is used as a fallback.
             */
            if (response.getLatencyMs() == null) {

                response.setLatencyMs(
                        latencyMs
                );
            }

            if (response.getProvider() == null
                    || response.getProvider().isBlank()) {

                response.setProvider(
                        llmProvider.getProviderCode()
                );
            }

            if (response.getModel() == null
                    || response.getModel().isBlank()) {

                response.setModel(
                        llmProperties.getModel()
                );
            }

            log.info(
                    "LLM generation completed successfully. callId={}, provider={}, model={}, latencyMs={}, inputTokens={}, outputTokens={}, totalTokens={}",
                    request.getCallId(),
                    response.getProvider(),
                    response.getModel(),
                    response.getLatencyMs(),
                    response.getInputTokens(),
                    response.getOutputTokens(),
                    response.getTotalTokens()
            );

            /*
             * Do not log the generated content because the response may
             * contain sensitive caller information.
             */
            log.debug(
                    "LLM response generated. callId={}, responseContentLength={}",
                    request.getCallId(),
                    response.getContent() == null
                            ? 0
                            : response.getContent().length()
            );

            return response;

        } catch (BadRequestException exception) {

            log.warn(
                    "LLM generation validation failed. callId={}, reason={}",
                    request.getCallId(),
                    exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            long latencyMs =
                    System.currentTimeMillis()
                            - startTime;

            log.error(
                    "LLM generation failed. callId={}, latencyMs={}",
                    request.getCallId(),
                    latencyMs,
                    exception
            );

            throw new IllegalStateException(
                    LlmMessages.GENERATION_FAILED,
                    exception
            );
        }
    }

    /**
     * Validates the LLM generation request.
     *
     * @param request LLM generation request
     */
    private void validateRequest(
            LlmGenerationRequestDto request) {

        if (request == null) {

            throw new BadRequestException(
                    LlmMessages
                            .GENERATION_REQUEST_REQUIRED
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new BadRequestException(
                    LlmMessages.CALL_ID_REQUIRED
            );
        }

        if (request.getLanguage() == null
                || request.getLanguage().isBlank()) {

            throw new BadRequestException(
                    LlmMessages.LANGUAGE_REQUIRED
            );
        }

        if (request.getMessages() == null
                || request.getMessages().isEmpty()) {

            throw new BadRequestException(
                    LlmMessages.MESSAGES_REQUIRED
            );
        }
    }

    /**
     * Validates the requested language against the configured
     * supported language list.
     *
     * @param language requested language
     */
    private void validateLanguage(
            String language) {

        List<String> supportedLanguages =
                llmProperties
                        .getSupportedLanguages();

        if (supportedLanguages == null
                || supportedLanguages.isEmpty()) {

            log.warn(
                    "No LLM supported languages are configured."
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
                    "Unsupported LLM language requested. language={}",
                    language
            );

            throw new BadRequestException(
                    LlmMessages
                            .LANGUAGE_NOT_SUPPORTED
            );
        }
    }

    /**
     * Validates all conversation messages.
     *
     * @param messages conversation messages
     * @param callId call identifier used for logging
     */
    private void validateMessages(
            List<LlmMessageDto> messages,
            String callId) {

        for (LlmMessageDto message : messages) {

            if (message == null) {

                log.warn(
                        "Null LLM message received. callId={}",
                        callId
                );

                throw new BadRequestException(
                        LlmMessages
                                .MESSAGE_CONTENT_REQUIRED
                );
            }

            log.debug(
                    "Validating LLM message. callId={}, role={}, contentLength={}",
                    callId,
                    message.getRole(),
                    message.getContent() == null
                            ? 0
                            : message.getContent().length()
            );

            if (message.getRole() == null
                    || message.getRole().isBlank()) {

                throw new BadRequestException(
                        LlmMessages
                                .MESSAGE_ROLE_REQUIRED
                );
            }

            if (message.getContent() == null
                    || message.getContent().isBlank()) {

                throw new BadRequestException(
                        LlmMessages
                                .MESSAGE_CONTENT_REQUIRED
                );
            }
        }
    }

    /**
     * Validates that the configured LLM provider is available.
     */
    private void validateProvider() {

        if (llmProvider == null) {

            log.error(
                    "LLM provider is not configured."
            );

            throw new IllegalStateException(
                    LlmMessages
                            .PROVIDER_NOT_CONFIGURED
            );
        }

        if (!llmProvider.isAvailable()) {

            log.error(
                    "Configured LLM provider is unavailable. provider={}",
                    llmProvider.getProviderCode()
            );

            throw new IllegalStateException(
                    LlmMessages
                            .PROVIDER_UNAVAILABLE
            );
        }
    }
}