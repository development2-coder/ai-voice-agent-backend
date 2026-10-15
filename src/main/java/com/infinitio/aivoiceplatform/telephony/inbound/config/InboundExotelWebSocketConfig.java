package com.infinitio.aivoiceplatform.telephony.inbound.config;

import com.infinitio.aivoiceplatform.telephony.inbound.websocket.InboundExotelWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for inbound Exotel calls.
 *
 * <p>
 * Registers the inbound Exotel WebSocket endpoint when inbound
 * calling is enabled through application configuration.
 * </p>
 *
 * <p>
 * Existing outbound Exotel WebSocket configuration is not modified.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class InboundExotelWebSocketConfig
        implements WebSocketConfigurer {

    /**
     * Inbound telephony configuration.
     */
    private final InboundTelephonyProperties
            inboundTelephonyProperties;

    /**
     * Inbound Exotel WebSocket handler.
     */
    private final InboundExotelWebSocketHandler
            inboundExotelWebSocketHandler;

    /**
     * Registers the inbound Exotel WebSocket endpoint.
     *
     * @param registry Spring WebSocket handler registry
     */
    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry) {

        if (!Boolean.TRUE.equals(
                inboundTelephonyProperties.getEnabled())) {

            log.info(
                    "Inbound telephony WebSocket is disabled. " +
                            "No inbound Exotel WebSocket endpoint will " +
                            "be registered."
            );

            return;
        }

        String websocketPath =
                inboundTelephonyProperties
                        .getWebsocketPath();

        if (websocketPath == null
                || websocketPath.isBlank()) {

            throw new IllegalStateException(
                    "Inbound telephony WebSocket path is required "
                            + "when inbound calling is enabled."
            );
        }

        registry.addHandler(
                inboundExotelWebSocketHandler,
                websocketPath
        );

        log.info(
                "Inbound Exotel WebSocket endpoint registered. " +
                        "path={}, provider={}",
                websocketPath,
                inboundTelephonyProperties
                        .getProvider()
        );
    }
}