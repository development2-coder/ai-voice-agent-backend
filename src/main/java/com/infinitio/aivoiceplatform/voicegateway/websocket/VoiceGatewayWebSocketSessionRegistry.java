package com.infinitio.aivoiceplatform.voicegateway.websocket;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.voicegateway.dto.response.VoiceGatewayResponseDto;
import com.infinitio.aivoiceplatform.voicegateway.mapper.VoiceGatewayResponseMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;

import org.springframework.web.socket.CloseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Maintains active Voice Gateway WebSocket sessions.
 *
 * <p>
 * Streaming STT results are delivered asynchronously. This registry
 * allows the runtime to send conversational responses and streaming
 * TTS audio back through the correct provider WebSocket session.
 * </p>
 *
 * <p>
 * The registry is intentionally in-memory and does not use Redis.
 * </p>
 *
 * <p>
 * The registry is transport-neutral. Provider-specific message
 * formatting is delegated to {@link VoiceGatewayResponseMapper}.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceGatewayWebSocketSessionRegistry {

    private final ObjectMapper objectMapper;

    private final VoiceGatewayResponseMapper responseMapper;

    private final Map<String, WebSocketSession>
            sessions =
            new ConcurrentHashMap<>();

    /**
     * Registers a WebSocket session for a call.
     *
     * @param callId application call identifier
     * @param session WebSocket session
     */
    public void register(
            String callId,
            WebSocketSession session) {

        if (callId == null
                || callId.isBlank()
                || session == null) {

            log.warn(
                    "Ignoring invalid Voice Gateway session registration. " +
                            "callId={}, sessionPresent={}",
                    callId,
                    session != null
            );

            return;
        }

        sessions.put(
                callId,
                session
        );

        log.debug(
                "Voice Gateway session registered. " +
                        "callId={}, sessionId={}",
                callId,
                session.getId()
        );
    }

    /**
     * Removes the WebSocket session associated with a call.
     *
     * @param callId application call identifier
     */
    public void remove(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        WebSocketSession session =
                sessions.remove(
                        callId
                );

        if (session != null) {

            log.debug(
                    "Voice Gateway session removed. " +
                            "callId={}, sessionId={}",
                    callId,
                    session.getId()
            );
        }
    }

    /**
     * Sends a provider-neutral Voice Gateway response through
     * the WebSocket associated with a call.
     *
     * @param callId application call identifier
     * @param response gateway response
     */
    public void send(
            String callId,
            VoiceGatewayResponseDto response) {

        if (response == null) {

            log.debug(
                    "Ignoring null Voice Gateway response. callId={}",
                    callId
            );

            return;
        }

        WebSocketSession session =
                getOpenSession(
                        callId
                );

        if (session == null) {

            return;
        }

        try {

            String payload =
                    responseMapper.toProviderMessage(
                            response
                    );

            if (payload == null
                    || payload.isBlank()) {

                log.debug(
                        "Voice Gateway response mapper returned empty payload. " +
                                "callId={}, sessionId={}",
                        callId,
                        session.getId()
                );

                return;
            }

            sendText(
                    session,
                    payload
            );

            log.debug(
                    "Asynchronous Voice Gateway response sent. " +
                            "callId={}, sessionId={}, action={}",
                    callId,
                    session.getId(),
                    response.getAction()
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to send asynchronous Voice Gateway response. " +
                            "callId={}, sessionId={}",
                    callId,
                    session.getId(),
                    exception
            );
        }
    }

    /**
     * Sends a streaming TTS audio chunk through the active
     * provider WebSocket session.
     *
     * <p>
     * Each chunk is encoded as Base64 and converted into the
     * existing provider-neutral media response. The response
     * mapper is responsible for converting that response into
     * the actual provider transport message.
     * </p>
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     * @param audioBytes audio chunk
     * @param contentType audio content type
     */
    public void sendAudio(
            String callId,
            String streamId,
            byte[] audioBytes,
            String contentType) {

        if (callId == null
                || callId.isBlank()) {

            log.warn(
                    "Cannot send TTS audio because callId is missing."
            );

            return;
        }

        if (streamId == null
                || streamId.isBlank()) {

            log.warn(
                    "Cannot send TTS audio because streamId is missing. " +
                            "callId={}",
                    callId
            );

            return;
        }

        if (audioBytes == null
                || audioBytes.length == 0) {

            log.debug(
                    "Ignoring empty TTS audio chunk. " +
                            "callId={}, streamId={}",
                    callId,
                    streamId
            );

            return;
        }

        WebSocketSession session =
                getOpenSession(
                        callId
                );

        if (session == null) {

            return;
        }

        try {

            String audioBase64 =
                    Base64.getEncoder()
                            .encodeToString(
                                    audioBytes
                            );

            VoiceGatewayResponseDto response =
                    VoiceGatewayResponseDto.builder()
                            .callId(
                                    callId
                            )
                            .streamId(
                                    streamId
                            )
                            .action(
                                    "MEDIA"
                            )
                            .audioBase64(
                                    audioBase64
                            )
                            .contentType(
                                    contentType
                            )
                            .build();

            String payload =
                    responseMapper.toProviderMessage(
                            response
                    );

            if (payload == null
                    || payload.isBlank()) {

                log.warn(
                        "Unable to create provider audio payload. " +
                                "callId={}, streamId={}",
                        callId,
                        streamId
                );

                return;
            }

            sendText(
                    session,
                    payload
            );

            log.debug(
                    "Streaming TTS audio chunk sent. " +
                            "callId={}, streamId={}, sessionId={}, " +
                            "audioBytes={}, contentType={}",
                    callId,
                    streamId,
                    session.getId(),
                    audioBytes.length,
                    contentType
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to send streaming TTS audio. " +
                            "callId={}, streamId={}, audioBytes={}",
                    callId,
                    streamId,
                    audioBytes.length,
                    exception
            );
        }
    }

    /**
     * Clears already queued provider audio for a call.
     *
     * <p>
     * This method is used during caller barge-in. The Voice
     * Gateway response mapper converts the clear-audio response
     * into the provider-specific clear-media command.
     * </p>
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     */
    public void clearAudio(
            String callId,
            String streamId) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        if (streamId == null
                || streamId.isBlank()) {

            log.debug(
                    "Cannot clear provider audio because streamId " +
                            "is missing. callId={}",
                    callId
            );

            return;
        }

        WebSocketSession session =
                getOpenSession(
                        callId
                );

        if (session == null) {

            return;
        }

        try {

            VoiceGatewayResponseDto response =
                    VoiceGatewayResponseDto.builder()
                            .callId(
                                    callId
                            )
                            .streamId(
                                    streamId
                            )
                            .action(
                                    "LISTEN"
                            )
                            .listen(
                                    true
                            )
                            .clearAudio(
                                    true
                            )
                            .build();

            String payload =
                    responseMapper.toProviderMessage(
                            response
                    );

            if (payload == null
                    || payload.isBlank()) {

                log.warn(
                        "Unable to create provider clear-audio payload. " +
                                "callId={}, streamId={}",
                        callId,
                        streamId
                );

                return;
            }

            sendText(
                    session,
                    payload
            );

            log.info(
                    "Provider audio clear command sent. " +
                            "callId={}, streamId={}, sessionId={}",
                    callId,
                    streamId,
                    session.getId()
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to clear provider audio. " +
                            "callId={}, streamId={}",
                    callId,
                    streamId,
                    exception
            );
        }
    }

    /**
     * Returns the active open WebSocket session for a call.
     *
     * @param callId application call identifier
     * @return open WebSocket session or null
     */
    private WebSocketSession getOpenSession(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return null;
        }

        WebSocketSession session =
                sessions.get(
                        callId
                );

        if (session == null) {

            log.warn(
                    "No active Voice Gateway session found. callId={}",
                    callId
            );

            return null;
        }

        if (!session.isOpen()) {

            log.warn(
                    "Voice Gateway session is already closed. " +
                            "callId={}, sessionId={}",
                    callId,
                    session.getId()
            );

            remove(
                    callId
            );

            return null;
        }

        return session;
    }

    /**
     * Sends a text message through a WebSocket session.
     *
     * <p>
     * WebSocket sends are synchronized because both asynchronous
     * TTS chunks and asynchronous conversation responses may be
     * sent concurrently for the same call.
     * </p>
     *
     * @param session WebSocket session
     * @param payload provider payload
     */
    private void sendText(
            WebSocketSession session,
            String payload)
            throws Exception {

        synchronized (session) {

            if (!session.isOpen()) {

                throw new IllegalStateException(
                        "Voice Gateway WebSocket session is closed."
                );
            }

            session.sendMessage(
                    new TextMessage(
                            payload
                    )
            );
        }
    }

    /**
     * Closes the active WebSocket session associated with a call.
     *
     * <p>
     * This method is used by runtime operations such as call transfer
     * when the active provider media stream must be terminated.
     * </p>
     *
     * @param callId application call identifier
     */
    public void close(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            log.warn(
                    "Cannot close Voice Gateway session because "
                            + "callId is missing."
            );

            return;
        }

        WebSocketSession session =
                sessions.get(
                        callId
                );

        if (session == null) {

            log.warn(
                    "No active Voice Gateway WebSocket session found. "
                            + "callId={}",
                    callId
            );

            return;
        }

        if (!session.isOpen()) {

            sessions.remove(
                    callId
            );

            log.debug(
                    "Voice Gateway WebSocket session was already closed. "
                            + "callId={}, sessionId={}",
                    callId,
                    session.getId()
            );

            return;
        }

        try {

            session.close(
                    CloseStatus.NORMAL
            );

            sessions.remove(
                    callId
            );

            log.info(
                    "Voice Gateway WebSocket session closed. "
                            + "callId={}, sessionId={}",
                    callId,
                    session.getId()
            );

        } catch (IOException exception) {

            log.error(
                    "Unable to close Voice Gateway WebSocket session. "
                            + "callId={}, sessionId={}",
                    callId,
                    session.getId(),
                    exception
            );
        }
    }
}