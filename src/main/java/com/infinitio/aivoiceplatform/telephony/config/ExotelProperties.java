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

    /**
     * Exotel API base URL.
     */
    private String baseUrl;

    /**
     * Exotel account SID.
     */
    private String accountSid;

    /**
     * Exotel API key.
     */
    private String apiKey;

    /**
     * Exotel API token.
     */
    private String apiToken;

    /**
     * Exotel virtual number / ExoPhone.
     */
    private String callerId;

    /**
     * Exotel application ID.
     */
    private String appId;

    /**
     * Exotel application flow URL.
     */
    private String appUrl;

    /**
     * Callback URL used by Exotel for call status updates.
     */
    private String statusCallbackUrl;
}