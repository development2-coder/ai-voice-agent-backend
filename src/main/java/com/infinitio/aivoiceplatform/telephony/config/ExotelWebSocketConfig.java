package com.infinitio.aivoiceplatform.telephony.config;

import com.infinitio.aivoiceplatform.telephony.websocket.exotel.ExotelWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configures the Exotel bidirectional WebSocket endpoint.
 *
 * <p>
 * This configuration registers the provider-specific Exotel media
 * streaming endpoint separately from the generic Voice Gateway
 * WebSocket endpoint.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ExotelWebSocketConfig
        implements WebSocketConfigurer {

    /**
     * Exotel WebSocket handler.
     */
    private final ExotelWebSocketHandler exotelWebSocketHandler;

    /**
     * Registers the Exotel media WebSocket endpoint.
     *
     * @param registry WebSocket handler registry
     */
    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry) {

        registry.addHandler(
                exotelWebSocketHandler,
                "/ws/telephony/exotel/media"
        );

        log.info(
                "Exotel WebSocket endpoint registered successfully. " +
                        "path=/ws/telephony/exotel/media"
        );
    }
}