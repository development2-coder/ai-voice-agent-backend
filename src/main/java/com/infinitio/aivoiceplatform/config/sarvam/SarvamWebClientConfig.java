package com.infinitio.aivoiceplatform.config.sarvam;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.infinitio.aivoiceplatform.tts.config.TtsProperties;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

/**
 * Configures the reusable WebClient used for Sarvam API communication.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TtsProperties.class)
public class SarvamWebClientConfig {

    private static final Duration DEFAULT_CONNECTION_TIMEOUT =
            Duration.ofSeconds(10);

    private static final Duration DEFAULT_RESPONSE_TIMEOUT =
            Duration.ofSeconds(30);

    /**
     * Creates the reusable WebClient for Sarvam API communication.
     *
     * @param ttsProperties TTS runtime configuration
     * @return configured WebClient
     */
    @Bean(name = "sarvamWebClient")
    public WebClient sarvamWebClient(
            TtsProperties ttsProperties) {

        Duration connectionTimeout =
                resolveConnectionTimeout(
                        ttsProperties.getConnectionTimeout()
                );

        Duration responseTimeout =
                resolveResponseTimeout(
                        ttsProperties.getResponseTimeout()
                );

        log.info(
                "Initializing Sarvam WebClient. endpoint={}, connectionTimeout={}, responseTimeout={}",
                ttsProperties.getEndpoint(),
                connectionTimeout,
                responseTimeout
        );

        HttpClient httpClient =
                HttpClient.create()
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                Math.toIntExact(
                                        connectionTimeout.toMillis()
                                )
                        )
                        .responseTimeout(
                                responseTimeout
                        );

        WebClient webClient =
                WebClient.builder()
                        .baseUrl(
                                ttsProperties.getEndpoint()
                        )
                        .clientConnector(
                                new ReactorClientHttpConnector(
                                        httpClient
                                )
                        )
                        .build();

        log.info(
                "Sarvam WebClient initialized successfully. endpoint={}",
                ttsProperties.getEndpoint()
        );

        return webClient;
    }

    /**
     * Resolves the configured connection timeout.
     *
     * @param configuredTimeout configured timeout
     * @return effective connection timeout
     */
    private Duration resolveConnectionTimeout(
            Duration configuredTimeout) {

        if (configuredTimeout == null
                || configuredTimeout.isZero()
                || configuredTimeout.isNegative()) {

            log.warn(
                    "Sarvam connection timeout is not configured. Using default {}.",
                    DEFAULT_CONNECTION_TIMEOUT
            );

            return DEFAULT_CONNECTION_TIMEOUT;
        }

        return configuredTimeout;
    }

    /**
     * Resolves the configured response timeout.
     *
     * @param configuredTimeout configured timeout
     * @return effective response timeout
     */
    private Duration resolveResponseTimeout(
            Duration configuredTimeout) {

        if (configuredTimeout == null
                || configuredTimeout.isZero()
                || configuredTimeout.isNegative()) {

            log.warn(
                    "Sarvam response timeout is not configured. Using default {}.",
                    DEFAULT_RESPONSE_TIMEOUT
            );

            return DEFAULT_RESPONSE_TIMEOUT;
        }

        return configuredTimeout;
    }
}