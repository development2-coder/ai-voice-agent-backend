package com.infinitio.aivoiceplatform.telephony.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for the platform-owned telephony media
 * streaming endpoint.
 *
 * <p>
 * This configuration is intentionally independent of any
 * CPaaS provider. The application owns the media endpoint,
 * while each CPaaS provider adapter is responsible for
 * mapping this endpoint to its own API.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "telephony.media")
public class TelephonyMediaProperties {

    /**
     * Public WebSocket endpoint used for real-time
     * bidirectional voice streaming.
     */
    private String streamUrl;
}