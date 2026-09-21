package com.infinitio.aivoiceplatform.voicegateway.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PreDestroy;

import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.telephony.constants.ExotelWebSocketConstants;
import com.infinitio.aivoiceplatform.voicegateway.dto.response.VoiceGatewayResponseDto;
import com.infinitio.aivoiceplatform.voicegateway.mapper.VoiceGatewayResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

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

    private final Map<String, String>
            streamIds =
            new ConcurrentHashMap<>();

    /**
     * Exotel receives live audio in real-time.
     *
     * <p>
     * For 8 kHz, 16-bit, mono Linear16 audio:
     *
     * <ul>
     *     <li>8000 samples/sec</li>
     *     <li>2 bytes/sample</li>
     *     <li>16000 bytes/sec</li>
     *     <li>3200 bytes = 200 ms audio</li>
     * </ul>
     *
     * Therefore each 3200-byte packet should be sent approximately
     * every 200 ms instead of sending the complete TTS response
     * as quickly as possible.
     */
    //private static final long OUTBOUND_AUDIO_PACKET_DELAY_MS = 200L;

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

        WebSocketSession decoratedSession =
                new ConcurrentWebSocketSessionDecorator(
                        session,
                        15000,
                        4 * 1024 * 1024
                );

        sessions.put(
                callId,
                decoratedSession
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

        AtomicLong generation =
                audioGeneration.get(
                        callId
                );

        if (generation != null) {

            generation.incrementAndGet();
        }

        nextAudioSendNanos.remove(
                callId
        );

        audioGeneration.remove(
                callId
        );

        streamIds.remove(
                callId
        );

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
     * Sarvam may provide audio chunks whose size does not match
     * the packet size required by Exotel. Therefore audio is first
     * accumulated in a per-call buffer and only complete Exotel
     * compatible packets are transmitted.
     *
     * <p>
     * Outbound packets are deliberately paced so that the server
     * does not write audio to the Exotel WebSocket faster than
     * the live call can consume it. This prevents blocking
     * WebSocket writes and Exotel write timeouts.
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
                || callId.isBlank()
                || streamId == null
                || streamId.isBlank()
                || audioBytes == null
                || audioBytes.length == 0) {

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

            byte[][] chunks;

            synchronized (buffer) {

                buffer.write(
                        audioBytes
                );

                int chunkSize =
                        ExotelWebSocketConstants
                                .OUTBOUND_AUDIO_CHUNK_SIZE_BYTES;

                byte[] allAudio =
                        buffer.toByteArray();

                int sendableBytes =
                        (allAudio.length / chunkSize)
                                * chunkSize;

                if (sendableBytes <= 0) {
                    return;
                }

                int chunkCount =
                        sendableBytes / chunkSize;

                chunks =
                        new byte[chunkCount][];

                for (int index = 0;
                     index < chunkCount;
                     index++) {

                    chunks[index] =
                            java.util.Arrays.copyOfRange(
                                    allAudio,
                                    index * chunkSize,
                                    (index + 1) * chunkSize
                            );
                }

                buffer.reset();

                if (sendableBytes < allAudio.length) {

                    buffer.write(
                            allAudio,
                            sendableBytes,
                            allAudio.length - sendableBytes
                    );
                }
            }

            for (byte[] chunk : chunks) {

                scheduleAudioChunk(
                        callId,
                        streamId,
                        chunk,
                        contentType
                );
            }

        } catch (Exception exception) {

            log.error(
                    "Unable to queue streaming TTS audio. " +
                            "callId={}, streamId={}, audioBytes={}",
                    callId,
                    streamId,
                    audioBytes.length,
                    exception
            );
        }
    }

    private void scheduleAudioChunk(
            String callId,
            String streamId,
            byte[] audioChunk,
            String contentType) {

        AtomicLong nextSend =
                nextAudioSendNanos.computeIfAbsent(
                        callId,
                        key -> new AtomicLong(
                                System.nanoTime()
                        )
                );

        AtomicLong generation =
                audioGeneration.computeIfAbsent(
                        callId,
                        key -> new AtomicLong(0L)
                );

        long currentGeneration =
                generation.get();

        long now =
                System.nanoTime();

        long scheduledTime =
                nextSend.getAndUpdate(
                        previous -> {

                            long base =
                                    Math.max(
                                            previous,
                                            now
                                    );

                            return base
                                    + TimeUnit.MILLISECONDS
                                    .toNanos(
                                            OUTBOUND_AUDIO_PACKET_DELAY_MS
                                    );
                        }
                );

        long delayNanos =
                Math.max(
                        0L,
                        scheduledTime - now
                );

        outboundAudioScheduler.schedule(
                () -> {

                    if (generation.get()
                            != currentGeneration) {

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

                        sendAudioChunk(
                                callId,
                                streamId,
                                session,
                                audioChunk,
                                contentType
                        );

                    } catch (Exception exception) {

                        log.error(
                                "Unable to send scheduled TTS audio. " +
                                        "callId={}, streamId={}",
                                callId,
                                streamId,
                                exception
                        );
                    }
                },
                delayNanos,
                TimeUnit.NANOSECONDS
        );
    }

    /**
     * Flushes the remaining TTS audio for a completed response.
     *
     * <p>
     * Any remaining Linear16 PCM audio is padded with silence so
     * that the final packet has the configured Exotel packet size.
     *
     * @param callId application call identifier
     * @param contentType audio content type
     */
    public void flushAudio(
            String callId,
            String contentType) {

        String streamId =
                streamIds.get(
                        callId
                );

        flushAudio(
                callId,
                streamId,
                contentType
        );
    }

    /**
     * Flushes the remaining TTS audio using the supplied stream ID.
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
                outboundAudioBuffers.remove(
                        callId
                );

        if (buffer == null) {
            return;
        }

        byte[] remainingAudio;

        synchronized (buffer) {

            remainingAudio =
                    buffer.toByteArray();

            buffer.reset();
        }

        if (remainingAudio.length == 0) {
            return;
        }

        int chunkSize =
                ExotelWebSocketConstants
                        .OUTBOUND_AUDIO_CHUNK_SIZE_BYTES;

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

        scheduleAudioChunk(
                callId,
                streamId,
                finalChunk,
                contentType
        );

        log.debug(
                "Final TTS audio packet queued. " +
                        "callId={}, streamId={}, " +
                        "remainingAudioBytes={}",
                callId,
                streamId,
                remainingAudio.length
        );
    }

    /**
     * Sends one audio chunk to the provider WebSocket.
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     * @param session active WebSocket session
     * @param audioChunk audio chunk
     * @param contentType audio content type
     * @throws Exception when the WebSocket cannot send the message
     */
    private void sendAudioChunk(
            String callId,
            String streamId,
            WebSocketSession session,
            byte[] audioChunk,
            String contentType)
            throws Exception {

        if (!session.isOpen()) {

            throw new IllegalStateException(
                    "Voice Gateway WebSocket session is closed."
            );
        }

        String audioBase64 =
                Base64.getEncoder()
                        .encodeToString(
                                audioChunk
                        );
        log.info(
                "EXOTEL OUTBOUND AUDIO CHECK. " +
                        "callId={}, streamId={}, sessionOpen={}, " +
                        "audioBytes={}, base64Length={}, contentType={}",
                callId,
                streamId,
                session != null && session.isOpen(),
                audioChunk.length,
                audioBase64.length(),
                contentType
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

        log.debug(
                "Sending TTS audio to Exotel. " +
                        "callId={}, streamId={}, sessionId={}, " +
                        "audioBytes={}, base64Bytes={}, payloadBytes={}",
                callId,
                streamId,
                session.getId(),
                audioChunk.length,
                audioBase64.length(),
                payload.length()
        );

        sendText(
                session,
                payload
        );

        log.debug(
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
     * This method is used during caller barge-in.
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

        AtomicLong generation =
                audioGeneration.computeIfAbsent(
                        callId,
                        key -> new AtomicLong(0L)
                );

        generation.incrementAndGet();

        nextAudioSendNanos.put(
                callId,
                new AtomicLong(
                        System.nanoTime()
                )
        );

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
     * Spring's standard WebSocket session does not support multiple
     * concurrent writers safely. TTS audio, clear-audio commands,
     * and other gateway responses may be produced by different
     * application threads. Therefore all writes for the same
     * WebSocket session are serialized here.
     *
     * @param session WebSocket session
     * @param payload provider payload
     * @throws Exception when the WebSocket write fails
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
     * @param callId application call identifier
     */
    public void close(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            log.warn(
                    "Cannot close Voice Gateway session because " +
                            "callId is missing."
            );

            return;
        }

        WebSocketSession session =
                sessions.get(
                        callId
                );

        if (session == null) {

            log.warn(
                    "No active Voice Gateway WebSocket session found. " +
                            "callId={}",
                    callId
            );

            return;
        }

        if (!session.isOpen()) {

            sessions.remove(
                    callId
            );

            log.debug(
                    "Voice Gateway WebSocket session was already closed. " +
                            "callId={}, sessionId={}",
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
                    "Voice Gateway WebSocket session closed. " +
                            "callId={}, sessionId={}",
                    callId,
                    session.getId()
            );

        } catch (IOException exception) {

            log.error(
                    "Unable to close Voice Gateway WebSocket session. " +
                            "callId={}, sessionId={}",
                    callId,
                    session.getId(),
                    exception
            );
        }
    }

    /**
     * Registers the provider stream ID for a call.
     *
     * @param callId application call identifier
     * @param streamId provider stream identifier
     */
    public void registerStreamId(
            String callId,
            String streamId) {

        if (callId == null
                || callId.isBlank()
                || streamId == null
                || streamId.isBlank()) {

            return;
        }

        streamIds.put(
                callId,
                streamId
        );
    }

    private final ScheduledExecutorService
            outboundAudioScheduler =
            Executors.newScheduledThreadPool(
                    4,
                    runnable -> {

                        Thread thread =
                                new Thread(
                                        runnable,
                                        "exotel-audio-sender"
                                );

                        thread.setDaemon(true);

                        return thread;
                    }
            );

    private final Map<String, AtomicLong>
            nextAudioSendNanos =
            new ConcurrentHashMap<>();

    private final Map<String, AtomicLong>
            audioGeneration =
            new ConcurrentHashMap<>();

    private static final long
            OUTBOUND_AUDIO_PACKET_DELAY_MS = 100L;

    @PreDestroy
    public void shutdownAudioScheduler() {

        outboundAudioScheduler.shutdownNow();
    }
}