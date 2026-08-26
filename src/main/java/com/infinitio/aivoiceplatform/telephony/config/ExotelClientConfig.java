package com.infinitio.aivoiceplatform.telephony.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

/**
 * Configuration for Exotel REST client.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class ExotelClientConfig {

    private final ExotelProperties exotelProperties;

    /**
     * Creates the REST client used for Exotel API communication.
     *
     * @return configured RestClient
     */
    @Bean
    public RestClient exotelRestClient() {

        java.net.http.HttpClient httpClient =
                java.net.http.HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(30)
                        )
                        .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(
                        httpClient
                );

        requestFactory.setReadTimeout(
                Duration.ofSeconds(60)
        );

        return RestClient.builder()
                .baseUrl(
                        exotelProperties.getBaseUrl()
                )
                .requestFactory(
                        requestFactory
                )
                .build();
    }
}