package com.infinitio.aivoiceplatform.stt.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides REST client configuration for the STT module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class RestClientConfig {

    private final SttProperties sttProperties;

    /**
     * Creates STT REST client configuration.
     *
     * @param sttProperties STT configuration
     */
    public RestClientConfig(
            SttProperties sttProperties) {

        this.sttProperties = sttProperties;
    }

    /**
     * Creates the REST client builder used by STT providers.
     *
     * @return configured REST client builder
     */
    @Bean
    public RestClient.Builder restClientBuilder() {

        Duration timeout =
                sttProperties.getTimeout();

        SimpleClientHttpRequestFactory
                requestFactory =
                new SimpleClientHttpRequestFactory();

        int timeoutMillis =
                Math.toIntExact(
                        timeout.toMillis()
                );

        requestFactory.setConnectTimeout(
                timeoutMillis
        );

        requestFactory.setReadTimeout(
                timeoutMillis
        );

        log.info(
                "Initializing STT REST client. timeout={}",
                timeout
        );

        return RestClient.builder()
                .requestFactory(requestFactory);
    }
}