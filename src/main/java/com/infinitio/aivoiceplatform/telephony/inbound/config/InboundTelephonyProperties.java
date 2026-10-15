package com.infinitio.aivoiceplatform.telephony.inbound.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for inbound telephony.
 *
 * <p>
 * Values are externalized through environment variables and
 * application configuration. No deployment-specific values or
 * credentials should be hardcoded in Java source code.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "telephony.inbound")
public class InboundTelephonyProperties {

    /**
     * WebSocket endpoint path exposed by the application.
     *
     * <p>
     * Example:
     * /ws/telephony/exotel/inbound
     * </p>
     */
    private String websocketPath;

    /**
     * Public WebSocket URL used by Exotel.
     *
     * <p>
     * Example:
     * wss://voice.example.com/ws/telephony/exotel/inbound
     * </p>
     */
    private String websocketUrl;

    /**
     * Telephony provider.
     */
    private String provider;

    /**
     * Default audio sample rate for inbound telephony.
     */
    private Integer sampleRate;

    /**
     * Default audio encoding for inbound telephony.
     */
    private String audioEncoding;

    /**
     * Default number of audio channels.
     */
    private Integer channels;

    /**
     * Whether inbound calling is enabled.
     */
    private Boolean enabled;
}