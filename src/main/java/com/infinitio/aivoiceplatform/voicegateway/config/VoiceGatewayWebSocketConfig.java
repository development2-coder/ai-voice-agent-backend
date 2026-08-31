package com.infinitio.aivoiceplatform.voicegateway.config;

import com.infinitio.aivoiceplatform.voicegateway.websocket.VoiceGatewayWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for the Voice Gateway.
 *
 * <p>
 * Registers the real-time WebSocket endpoint used by the
 * telephony provider to exchange bidirectional voice media
 * with the AI voice platform.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class VoiceGatewayWebSocketConfig
        implements WebSocketConfigurer {

    private final VoiceGatewayWebSocketHandler
            voiceGatewayWebSocketHandler;

    /**
     * Registers the Voice Gateway WebSocket endpoint.
     *
     * @param registry WebSocket handler registry
     */
    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry) {

        log.info(
                "Registering Voice Gateway WebSocket endpoint. " +
                        "path=/voice-gateway/ws"
        );

        registry.addHandler(
                        voiceGatewayWebSocketHandler,
                        "/voice-gateway/ws"
                )
                .setAllowedOriginPatterns("*");

        log.info(
                "Voice Gateway WebSocket endpoint registered successfully."
        );
    }
}