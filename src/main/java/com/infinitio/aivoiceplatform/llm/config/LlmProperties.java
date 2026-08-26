package com.infinitio.aivoiceplatform.llm.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains runtime configuration properties for the LLM module.
 *
 * <p>
 * Provider-specific values are loaded from external configuration
 * and environment variables. No API credentials or environment-specific
 * values are hardcoded in the application.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "llm.runtime")
public class LlmProperties {

    /**
     * LLM provider code.
     */
    private String provider;

    /**
     * LLM provider API endpoint.
     */
    private String endpoint;

    /**
     * LLM provider API key.
     */
    private String apiKey;

    /**
     * API authentication header name.
     */
    private String apiKeyHeader;

    /**
     * LLM model identifier.
     */
    private String model;

    /**
     * LLM temperature.
     */
    private Double temperature;

    /**
     * Top-p sampling value.
     */
    private Double topP;

    /**
     * Maximum number of output tokens.
     */
    private Integer maxTokens;

    /**
     * Indicates whether streaming is enabled.
     */
    private Boolean stream;

    /**
     * Sarvam reasoning effort.
     *
     * <p>
     * Supported values are provider-defined, such as low, medium,
     * or high. A blank value can be used when reasoning is disabled.
     * </p>
     */
    private String reasoningEffort;

    /**
     * Maximum time allowed for an LLM request.
     */
    private Duration timeout;

    /**
     * Languages supported by the configured LLM runtime.
     */
    private List<String> supportedLanguages;

    /**
     * Connection timeout for Sarvam API requests.
     */
    private Duration connectionTimeout;

    /**
     * Response timeout for Sarvam API requests.
     */
    private Duration responseTimeout;
}