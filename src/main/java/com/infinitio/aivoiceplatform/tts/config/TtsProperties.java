package com.infinitio.aivoiceplatform.tts.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains runtime configuration properties for the TTS module.
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
@ConfigurationProperties(prefix = "tts.runtime")
public class TtsProperties {

    /**
     * TTS provider code.
     */
    private String provider;

    /**
     * TTS provider API endpoint.
     */
    private String endpoint;

    /**
     * TTS provider API key.
     */
    private String apiKey;

    /**
     * API authentication header name.
     */
    private String apiKeyHeader;

    /**
     * TTS model identifier.
     */
    private String model;

    /**
     * Default speaker used when no speaker is supplied.
     */
    private String defaultSpeaker;

    /**
     * Default target language.
     */
    private String defaultLanguage;

    /**
     * Default speech pace.
     */
    private Double defaultPace;

    /**
     * Default speech sample rate.
     */
    private Integer defaultSpeechSampleRate;

    /**
     * Maximum number of characters allowed in one synthesis request.
     */
    private Integer maxTextCharacters;

    /**
     * Maximum supported speech pace.
     */
    private Double maxPace;

    /**
     * Minimum supported speech pace.
     */
    private Double minPace;

    /**
     * Maximum number of output audio bytes allowed.
     */
    private Long maxAudioSizeBytes;

    /**
     * Maximum time allowed for a TTS request.
     */
    private Duration timeout;

    /**
     * Connection timeout for provider requests.
     */
    private Duration connectionTimeout;

    /**
     * Response timeout for provider requests.
     */
    private Duration responseTimeout;

    /**
     * Languages supported by the configured TTS provider.
     */
    private List<String> supportedLanguages;

    /**
     * Supported speakers grouped by gender.
     *
     * <p>
     * Example:
     * male -> shubh, ratan, aditya
     * female -> priya, ishita, ritu
     * </p>
     */
    private Map<String, List<String>> supportedSpeakers;

    /**
     * Server directory where generated TTS audio files are stored.
     */
    private String audioStoragePath;

    /**
     * Base URL used to access generated TTS audio files.
     */
    private String audioBaseUrl;
}