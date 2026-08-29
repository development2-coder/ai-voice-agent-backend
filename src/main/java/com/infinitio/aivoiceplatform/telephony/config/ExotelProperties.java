package com.infinitio.aivoiceplatform.telephony.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for Exotel telephony integration.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "exotel")
public class ExotelProperties {

    private String baseUrl;

    private String accountSid;

    private String apiKey;

    private String apiToken;

    private String callerId;

    private String appId;

    private String appUrl;

    private String statusCallbackUrl;

    /**
     * Public WSS endpoint used for real-time
     * bidirectional voice streaming.
     */
    private String streamUrl;

    /**
     * Expected value:
     *
     * bidirectional
     */
    private String streamType = "bidirectional";

    /**
     * Record outbound calls.
     */
    private Boolean record = true;

    /**
     * single / dual
     */
    private String recordingChannels = "single";

    /**
     * Optional maximum call duration in seconds.
     */
    private Integer timeLimit;

    /**
     * Secret used to protect the public WebSocket endpoint.
     */
    private String streamSecret;
}