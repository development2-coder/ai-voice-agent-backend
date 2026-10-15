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
     * API path used to retrieve completed call details,
     * including recording information.
     */
    private String callDetailsPath;

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

    private String availablePhoneNumbersPath;

    private String incomingPhoneNumbersPath;

    /**
     * API path used to retrieve active legs for an ongoing call.
     */
    private String activeCallLegsPath;

    /**
     * API path used to perform an action on a specific call leg.
     */
    private String callLegPath;

    private String callLegsPath;

    /**
     * Base URL for Exotel CCM APIs.
     *
     * <p>
     * Used for Call Legs APIs on the Singapore cluster.
     * </p>
     */
    private String ccmBaseUrl;
}