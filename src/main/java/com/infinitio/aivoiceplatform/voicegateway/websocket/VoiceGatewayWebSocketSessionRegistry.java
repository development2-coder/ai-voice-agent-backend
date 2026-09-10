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
import com.infinitio.aivoiceplatform.telephony.constants.ExotelWebSocketConstants;
import org.springframework.web.socket.CloseStatus;
import java.io.ByteArrayOutputStream;
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

    private final Map<String, ByteArrayOutputStream>
            outboundAudioBuffers =
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

        ByteArrayOutputStream buffer =
                outboundAudioBuffers.remove(
                        callId
                );

        if (buffer != null) {

            synchronized (buffer) {

                buffer.reset();
            }
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
    /**
     * Sends a streaming TTS audio chunk through the active
     * provider WebSocket session.
     *
     * <p>
     * Provider generated audio is split into smaller chunks
     * before being sent through the Exotel WebSocket.
     * </p>
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     * @param audioBytes audio bytes
     * @param contentType audio content type
     */
    /**
     * Sends a streaming TTS audio chunk through the active
     * provider WebSocket session.
     *
     * <p>
     * Sarvam may provide audio chunks whose size does not match
     * the packet size required by Exotel. Therefore audio is first
     * accumulated in a per-call buffer and only complete Exotel
     * compatible packets are transmitted.
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

            ByteArrayOutputStream buffer =
                    outboundAudioBuffers.computeIfAbsent(
                            callId,
                            key -> new ByteArrayOutputStream()
                    );

            synchronized (buffer) {

                buffer.write(
                        audioBytes
                );

                byte[] bufferedAudio =
                        buffer.toByteArray();

                int chunkSize =
                        ExotelWebSocketConstants
                                .OUTBOUND_AUDIO_CHUNK_SIZE_BYTES;

                int sendableBytes =
                        (bufferedAudio.length / chunkSize)
                                * chunkSize;

                if (sendableBytes <= 0) {

                    log.debug(
                            "TTS audio buffered waiting for complete " +
                                    "Exotel packet. callId={}, " +
                                    "bufferedBytes={}, requiredChunkBytes={}",
                            callId,
                            bufferedAudio.length,
                            chunkSize
                    );

                    return;
                }

                int offset = 0;

                int chunksSent = 0;

                while (offset < sendableBytes) {

                    byte[] audioChunk =
                            java.util.Arrays.copyOfRange(
                                    bufferedAudio,
                                    offset,
                                    offset + chunkSize
                            );

                    sendAudioChunk(
                            callId,
                            streamId,
                            session,
                            audioChunk,
                            contentType
                    );

                    offset += chunkSize;

                    chunksSent++;
                }

                buffer.reset();

                if (sendableBytes < bufferedAudio.length) {

                    buffer.write(
                            bufferedAudio,
                            sendableBytes,
                            bufferedAudio.length - sendableBytes
                    );
                }

                log.debug(
                        "Streaming TTS audio buffered and processed. " +
                                "callId={}, streamId={}, sourceBytes={}, " +
                                "bufferedBytes={}, chunksSent={}",
                        callId,
                        streamId,
                        audioBytes.length,
                        buffer.size(),
                        chunksSent
                );
            }

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
     * Flushes the remaining TTS audio for a completed response.
     *
     * <p>
     * Exotel requires outbound audio packets to use the configured
     * packet size. Any remaining Linear16 PCM audio is therefore
     * completed with silence so that the final packet remains a
     * valid Exotel packet.
     * </p>
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     * @param contentType audio content type
     */
    public void flushAudio(
            String callId,
            String streamId,
            String contentType) {

        if (callId == null
                || callId.isBlank()) {

            return;
        }

        ByteArrayOutputStream buffer =
                outboundAudioBuffers.get(
                        callId
                );

        if (buffer == null) {

            return;
        }

        WebSocketSession session =
                getOpenSession(
                        callId
                );

        if (session == null) {

            outboundAudioBuffers.remove(
                    callId
            );

            return;
        }

        try {

            synchronized (buffer) {

                int remainingBytes =
                        buffer.size();

                if (remainingBytes == 0) {

                    outboundAudioBuffers.remove(
                            callId
                    );

                    return;
                }

                int chunkSize =
                        ExotelWebSocketConstants
                                .OUTBOUND_AUDIO_CHUNK_SIZE_BYTES;

                byte[] remainingAudio =
                        buffer.toByteArray();

                byte[] finalChunk =
                        new byte[chunkSize];

                System.arraycopy(
                        remainingAudio,
                        0,
                        finalChunk,
                        0,
                        Math.min(
                                remainingAudio.length,
                                finalChunk.length
                        )
                );

                /*
                 * Remaining bytes after the actual audio are
                 * initialized to zero, which represents silence
                 * for Linear16 PCM.
                 */
                sendAudioChunk(
                        callId,
                        streamId,
                        session,
                        finalChunk,
                        contentType
                );

                buffer.reset();

                outboundAudioBuffers.remove(
                        callId
                );

                log.debug(
                        "Final TTS audio buffer flushed. " +
                                "callId={}, streamId={}, " +
                                "remainingAudioBytes={}, finalPacketBytes={}",
                        callId,
                        streamId,
                        remainingBytes,
                        finalChunk.length
                );
            }

        } catch (Exception exception) {

            log.error(
                    "Unable to flush final TTS audio buffer. " +
                            "callId={}, streamId={}",
                    callId,
                    streamId,
                    exception
            );
        }
    }

    /**
     * Sends one audio chunk to the provider WebSocket.
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     * @param session active WebSocket session
     * @param audioChunk audio chunk
     * @param contentType audio content type
     * @throws IOException when the WebSocket cannot send the message
     */
    private void sendAudioChunk(
            String callId,
            String streamId,
            WebSocketSession session,
            byte[] audioChunk,
            String contentType)
            throws Exception {

        String audioBase64 =
                Base64.getEncoder()
                        .encodeToString(
                                audioChunk
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

        log.info(
                "Streaming TTS audio chunk sent to Exotel. " +
                        "callId={}, streamId={}, sessionId={}, " +
                        "audioBytes={}, contentType={}",
                callId,
                streamId,
                session.getId(),
                audioChunk.length,
                contentType
        );
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

        ByteArrayOutputStream buffer =
                outboundAudioBuffers.remove(
                        callId
                );

        if (buffer != null) {

            synchronized (buffer) {

                buffer.reset();
            }

            log.debug(
                    "Discarded buffered TTS audio during barge-in. " +
                            "callId={}",
                    callId
            );
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