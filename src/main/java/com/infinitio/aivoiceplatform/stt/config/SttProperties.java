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

    // =========================================================
    // REALTIME STREAMING
    // =========================================================

    /**
     * Sarvam realtime streaming WebSocket endpoint.
     */
    private String streamingEndpoint;

    /**
     * Realtime streaming STT model.
     */
    private String streamingModel;

    /**
     * Realtime streaming STT mode.
     */
    private String streamingMode;

    /**
     * Enables server-side voice activity detection.
     */
    private boolean streamingVadEnabled;

    /**
     * Enables high VAD sensitivity.
     */
    private boolean streamingHighVadSensitivity;

    /**
     * Streaming endpointing mode.
     */
    private String streamingEndpointing;

    /**
     * Streaming cadence.
     */
    private String streamingStreamType;

    /**
     * Realtime streaming audio encoding.
     */
    private String streamingEncoding;

    /**
     * VAD threshold used by the streaming provider.
     */
    private Double streamingThreshold;

    /**
     * Duration of silence required to detect end of speech.
     */
    private Integer streamingSilenceDurationMs;

    /**
     * Minimum speech duration required for a speech segment.
     */
    private Integer streamingMinSpeechDurationMs;

    /**
     * Audio padding added before detected speech.
     */
    private Integer streamingPrefixPaddingMs;

    /**
     * Indicates whether provider timestamps should be returned.
     */
    private boolean streamingReturnTimestamps;

    /**
     * Logs loaded STT configuration metadata.
     *
     * <p>
     * Sensitive values such as API keys are never logged.
     * </p>
     */
    public void logConfiguration() {

        log.info(
                "STT configuration loaded. " +
                        "provider={}, timeout={}, " +
                        "maxAudioSizeBytes={}, sampleRate={}, " +
                        "streamingModel={}, streamingMode={}, " +
                        "streamingEndpointing={}, streamingStreamType={}, " +
                        "streamingEncoding={}, streamingVadEnabled={}, " +
                        "streamingHighVadSensitivity={}, " +
                        "supportedLanguageCount={}",
                provider,
                timeout,
                maxAudioSizeBytes,
                sampleRate,
                streamingModel,
                streamingMode,
                streamingEndpointing,
                streamingStreamType,
                streamingEncoding,
                streamingVadEnabled,
                streamingHighVadSensitivity,
                supportedLanguages != null
                        ? supportedLanguages.size()
                        : 0
        );
    }
}