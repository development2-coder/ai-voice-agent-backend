package com.infinitio.aivoiceplatform.stt.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Contains runtime configuration properties for the STT module.
 *
 * <p>
 * Provider-specific configuration is loaded from external configuration
 * and is not hardcoded in the application source code.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "voice.stt")
public class SttProperties {

    /**
     * Configured STT provider.
     */
    private String provider;

    /**
     * STT provider endpoint.
     */
    private String endpoint;

    /**
     * STT provider API key.
     */
    private String apiKey;

    /**
     * Provider request timeout.
     */
    private Duration timeout;

    /**
     * Maximum audio payload size in bytes.
     */
    private Long maxAudioSizeBytes;

    /**
     * Supported STT languages.
     */
    private List<String> supportedLanguages;

    /**
     * Sarvam STT model.
     */
    private String model;

    /**
     * Sarvam STT transcription mode.
     */
    private String mode;

    /**
     * Input audio sample rate.
     */
    private Integer sampleRate;

    /**
     * Logs the loaded STT configuration metadata.
     *
     * <p>
     * Sensitive configuration values such as the API key and endpoint
     * credentials are intentionally not logged.
     * </p>
     */
    public void logConfiguration() {

        log.info(
                "STT configuration loaded. provider={}, timeout={}, maxAudioSizeBytes={}, supportedLanguageCount={}",
                provider,
                timeout,
                maxAudioSizeBytes,
                supportedLanguages != null
                        ? supportedLanguages.size()
                        : 0
        );
    }
}