package com.infinitio.aivoiceplatform.voicegateway.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayConstants;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayMessages;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayDtmfRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayEventRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayMediaRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStartRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStopRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.response.VoiceGatewayResponseDto;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayCallResolverService;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * WebSocket handler for the Voice Gateway.
 *
 * <p>
 * Responsible only for WebSocket transport and event routing.
 * Business logic is delegated to VoiceGatewayService.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceGatewayWebSocketHandler
        extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    private final VoiceGatewayService voiceGatewayService;

    private final VoiceGatewayCallResolverService
            callResolverService;

    private final VoiceGatewayWebSocketSessionRegistry
            sessionRegistry;

    /**
     * Handles a newly established WebSocket connection.
     *
     * @param session WebSocket session
     */
    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        log.info(
                "Voice Gateway WebSocket connection established. " +
                        "sessionId={}",
                session.getId()
        );
    }

    /**
     * Handles an incoming WebSocket text message.
     *
     * @param session WebSocket session
     * @param message incoming message
     */
    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) {

        if (message == null
                || message.getPayload() == null
                || message.getPayload().isBlank()) {

            log.warn(
                    "Empty Voice Gateway WebSocket message. " +
                            "sessionId={}",
                    session.getId()
            );

            sendError(
                    session,
                    VoiceGatewayMessages.INVALID_EVENT
            );

            return;
        }

        String payload =
                message.getPayload();

        log.debug(
                "Voice Gateway WebSocket message received. " +
                        "sessionId={}, payloadLength={}",
                session.getId(),
                payload.length()
        );

        try {

            VoiceGatewayEventRequestDto event =
                    parseEvent(
                            payload
                    );

            handleEvent(
                    session,
                    event
            );

        } catch (IllegalArgumentException exception) {

            log.warn(
                    "Invalid Voice Gateway event. " +
                            "sessionId={}, message={}",
                    session.getId(),
                    exception.getMessage()
            );

            sendError(
                    session,
                    exception.getMessage()
            );

        } catch (Exception exception) {

            log.error(
                    "Unexpected Voice Gateway WebSocket error. " +
                            "sessionId={}",
                    session.getId(),
                    exception
            );

            sendError(
                    session,
                    VoiceGatewayMessages.UNEXPECTED_RUNTIME_ERROR
            );
        }
    }

    /**
     * Handles WebSocket transport errors.
     *
     * @param session WebSocket session
     * @param exception transport exception
     */
    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception) {

        log.error(
                "Voice Gateway WebSocket transport error. " +
                        "sessionId={}",
                session == null
                        ? null
                        : session.getId(),
                exception
        );
    }

    /**
     * Handles closed WebSocket connections.
     *
     * @param session WebSocket session
     * @param status close status
     */
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        log.info(
                "Voice Gateway WebSocket connection closed. " +
                        "sessionId={}, code={}, reason={}",
                session.getId(),
                status.getCode(),
                status.getReason()
        );
    }

    // =========================================================
    // EVENT PARSING
    // =========================================================

    /**
     * Parses the incoming provider WebSocket payload into the
     * normalized Voice Gateway event request.
     *
     * @param payload raw provider JSON payload
     * @return normalized Voice Gateway event request
     */
    private VoiceGatewayEventRequestDto parseEvent(
            String payload) {

        if (payload == null
                || payload.isBlank()) {

            log.warn(
                    "Voice Gateway event payload is empty."
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_EVENT
            );
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            payload
                    );

            String event =
                    textValue(
                            root,
                            VoiceGatewayConstants.FIELD_EVENT
                    );

            if (event == null
                    || event.isBlank()) {

                log.warn(
                        "Voice Gateway event type is missing."
                );

                throw new IllegalArgumentException(
                        VoiceGatewayMessages.EVENT_REQUIRED
                );
            }

            String streamId =
                    textValue(
                            root,
                            VoiceGatewayConstants.FIELD_STREAM_SID
                    );

            String providerCallId =
                    textValue(
                            root,
                            VoiceGatewayConstants.FIELD_CALL_SID
                    );

            Long sequenceNumber =
                    longValue(
                            root,
                            VoiceGatewayConstants.FIELD_SEQUENCE_NUMBER
                    );

            log.debug(
                    "Voice Gateway event parsed successfully. " +
                            "event={}, streamId={}, providerCallId={}, " +
                            "sequenceNumber={}",
                    event,
                    streamId,
                    providerCallId,
                    sequenceNumber
            );

            return VoiceGatewayEventRequestDto.builder()
                    .event(
                            event
                    )
                    .streamId(
                            streamId
                    )
                    .providerCallId(
                            providerCallId
                    )
                    .sequenceNumber(
                            sequenceNumber
                    )
                    .payload(
                            payload
                    )
                    .build();

        } catch (JsonProcessingException exception) {

            log.error(
                    "Unable to parse Voice Gateway WebSocket payload.",
                    exception
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_EVENT,
                    exception
            );
        }
    }

    // =========================================================
    // EVENT ROUTING
    // =========================================================

    /**
     * Routes an incoming event to the appropriate handler.
     *
     * @param session WebSocket session
     * @param event normalized event
     */
    private void handleEvent(
            WebSocketSession session,
            VoiceGatewayEventRequestDto event) {

        String eventType =
                event.getEvent();

        if (eventType == null
                || eventType.isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.EVENT_REQUIRED
            );
        }

        log.info(
                "Processing Voice Gateway event. " +
                        "sessionId={}, event={}, streamId={}, " +
                        "providerCallId={}",
                session.getId(),
                eventType,
                event.getStreamId(),
                event.getProviderCallId()
        );

        switch (
                eventType.toLowerCase()
        ) {

            case VoiceGatewayConstants.EVENT_CONNECTED ->
                    handleConnected(
                            session,
                            event
                    );

            case VoiceGatewayConstants.EVENT_START ->
                    handleStart(
                            session,
                            event
                    );

            case VoiceGatewayConstants.EVENT_MEDIA ->
                    handleMedia(
                            session,
                            event
                    );

            case VoiceGatewayConstants.EVENT_DTMF ->
                    handleDtmf(
                            session,
                            event
                    );

            case VoiceGatewayConstants.EVENT_MARK ->
                    handleMark(
                            session,
                            event
                    );

            case VoiceGatewayConstants.EVENT_STOP ->
                    handleStop(
                            session,
                            event
                    );

            default -> {

                log.warn(
                        "Unsupported Voice Gateway event. " +
                                "sessionId={}, event={}",
                        session.getId(),
                        eventType
                );

                throw new IllegalArgumentException(
                        VoiceGatewayMessages.UNSUPPORTED_EVENT
                );
            }
        }
    }

    // =========================================================
    // CONNECTED
    // =========================================================

    /**
     * Handles provider connection event.
     *
     * @param session WebSocket session
     * @param event connection event
     */
    private void handleConnected(
            WebSocketSession session,
            VoiceGatewayEventRequestDto event) {

        log.info(
                "Voice Gateway provider connection acknowledged. " +
                        "sessionId={}, streamId={}",
                session.getId(),
                event.getStreamId()
        );
    }

    // =========================================================
    // START
    // =========================================================

    /**
     * Handles provider START event.
     *
     * <p>
     * Provider call ID is resolved to the application's Call
     * public ID before entering the runtime.
     * </p>
     *
     * @param session WebSocket session
     * @param event start event
     */
    /**
     * Handles provider START event.
     *
     * <p>
     * Provider call ID is resolved to the application's Call
     * public ID before entering the runtime.
     * </p>
     *
     * @param session WebSocket session
     * @param event start event
     */
    private void handleStart(
            WebSocketSession session,
            VoiceGatewayEventRequestDto event) {

        String callId =
                resolveCallId(
                        event
                );

        sessionRegistry.register(
                callId,
                session
        );

        log.info(
                "Starting Voice Gateway call. " +
                        "callId={}, providerCallId={}, streamId={}",
                callId,
                event.getProviderCallId(),
                event.getStreamId()
        );

        VoiceGatewayStartRequestDto request =
                VoiceGatewayStartRequestDto.builder()
                        .callId(
                                callId
                        )
                        .providerCallId(
                                event.getProviderCallId()
                        )
                        .streamId(
                                event.getStreamId()
                        )
                        .build();

        VoiceGatewayResponseDto response =
                voiceGatewayService.startStream(
                        request
                );

        if (response != null) {

            sendResponse(
                    session,
                    response
            );
        }
    }

    // =========================================================
    // MEDIA
    // =========================================================

    /**
     * Handles incoming caller media.
     *
     * @param session WebSocket session
     * @param event media event
     */
    /**
     * Handles incoming caller media.
     *
     * @param session WebSocket session
     * @param event media event
     */
    private void handleMedia(
            WebSocketSession session,
            VoiceGatewayEventRequestDto event) {

        JsonNode root =
                readJson(
                        event.getPayload()
                );

        JsonNode media =
                root.get(
                        VoiceGatewayConstants.FIELD_MEDIA
                );

        if (media == null
                || media.isNull()) {

            log.warn(
                    "Media object is missing. " +
                            "sessionId={}, streamId={}",
                    session.getId(),
                    event.getStreamId()
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_MEDIA_EVENT
            );
        }

        String callId =
                resolveCallId(
                        event
                );

        String audioBase64 =
                textValue(
                        media,
                        VoiceGatewayConstants.FIELD_PAYLOAD
                );

        VoiceGatewayMediaRequestDto request =
                VoiceGatewayMediaRequestDto.builder()
                        .callId(
                                callId
                        )
                        .providerCallId(
                                event.getProviderCallId()
                        )
                        .streamId(
                                event.getStreamId()
                        )
                        .sequenceNumber(
                                event.getSequenceNumber()
                        )
                        .chunk(
                                longValue(
                                        media,
                                        VoiceGatewayConstants.FIELD_CHUNK
                                )
                        )
                        .timestamp(
                                longValue(
                                        media,
                                        VoiceGatewayConstants.FIELD_TIMESTAMP
                                )
                        )
                        .audioBase64(
                                audioBase64
                        )
                        .audioEncoding(
                                VoiceGatewayConstants.AUDIO_ENCODING
                        )
                        .sampleRate(
                                VoiceGatewayConstants.AUDIO_SAMPLE_RATE
                        )
                        .channels(
                                VoiceGatewayConstants.AUDIO_CHANNELS
                        )
                        .sampleSizeBits(
                                VoiceGatewayConstants.AUDIO_SAMPLE_SIZE_BITS
                        )
                        .inbound(
                                true
                        )
                        .valid(
                                true
                        )
                        .build();

        log.debug(
                "Caller media normalized. " +
                        "callId={}, streamId={}, chunk={}, " +
                        "audioPresent={}",
                callId,
                event.getStreamId(),
                request.getChunk(),
                audioBase64 != null
                        && !audioBase64.isBlank()
        );

        VoiceGatewayResponseDto response =
                voiceGatewayService.processMedia(
                        request
                );

        if (response != null) {

            sendResponse(
                    session,
                    response
            );
        }
    }

    // =========================================================
    // DTMF
    // =========================================================

    /**
     * Handles caller DTMF input.
     *
     * @param session WebSocket session
     * @param event DTMF event
     */
    private void handleDtmf(
            WebSocketSession session,
            VoiceGatewayEventRequestDto event) {

        JsonNode root =
                readJson(
                        event.getPayload()
                );

        JsonNode dtmf =
                root.get(
                        VoiceGatewayConstants.FIELD_DTMF
                );

        if (dtmf == null
                || dtmf.isNull()) {

            log.warn(
                    "DTMF object is missing. " +
                            "sessionId={}, streamId={}",
                    session.getId(),
                    event.getStreamId()
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_DTMF_EVENT
            );
        }

        String callId =
                resolveCallId(
                        event
                );

        String digit =
                textValue(
                        dtmf,
                        VoiceGatewayConstants.FIELD_DIGIT
                );

        VoiceGatewayDtmfRequestDto request =
                VoiceGatewayDtmfRequestDto.builder()
                        .callId(
                                callId
                        )
                        .providerCallId(
                                event.getProviderCallId()
                        )
                        .streamId(
                                event.getStreamId()
                        )
                        .sequenceNumber(
                                event.getSequenceNumber()
                        )
                        .digit(
                                digit
                        )
                        .timestamp(
                                longValue(
                                        dtmf,
                                        VoiceGatewayConstants.FIELD_TIMESTAMP
                                )
                        )
                        .build();

        log.info(
                "DTMF input normalized. " +
                        "callId={}, streamId={}, digit={}",
                callId,
                event.getStreamId(),
                digit
        );

        VoiceGatewayResponseDto response =
                voiceGatewayService.processDtmf(
                        request
                );

        sendResponse(
                session,
                response
        );
    }

    // =========================================================
    // MARK
    // =========================================================

    /**
     * Handles provider mark events.
     *
     * <p>
     * Marks are synchronization events and do not directly
     * execute Flow nodes.
     * </p>
     *
     * @param session WebSocket session
     * @param event mark event
     */
    private void handleMark(
            WebSocketSession session,
            VoiceGatewayEventRequestDto event) {

        JsonNode root =
                readJson(
                        event.getPayload()
                );

        JsonNode mark =
                root.get(
                        VoiceGatewayConstants.FIELD_MARK
                );

        String markName =
                mark == null
                        ? null
                        : textValue(
                        mark,
                        VoiceGatewayConstants.FIELD_MARK_NAME
                );

        log.debug(
                "Voice Gateway mark received. " +
                        "sessionId={}, streamId={}, mark={}",
                session.getId(),
                event.getStreamId(),
                markName
        );
    }

    // =========================================================
    // STOP
    // =========================================================

    /**
     * Handles provider STOP event.
     *
     * @param session WebSocket session
     * @param event stop event
     */
    private void handleStop(
            WebSocketSession session,
            VoiceGatewayEventRequestDto event) {

        JsonNode root =
                readJson(
                        event.getPayload()
                );

        String callId =
                resolveCallId(
                        event
                );

        VoiceGatewayStopRequestDto request =
                VoiceGatewayStopRequestDto.builder()
                        .callId(
                                callId
                        )
                        .providerCallId(
                                event.getProviderCallId()
                        )
                        .streamId(
                                event.getStreamId()
                        )
                        .sequenceNumber(
                                event.getSequenceNumber()
                        )
                        .reason(
                                textValue(
                                        root,
                                        VoiceGatewayConstants.FIELD_REASON
                                )
                        )
                        .timestamp(
                                longValue(
                                        root,
                                        VoiceGatewayConstants.FIELD_TIMESTAMP
                                )
                        )
                        .callTerminated(
                                true
                        )
                        .build();

        log.info(
                "Stopping Voice Gateway call. " +
                        "callId={}, streamId={}, reason={}",
                callId,
                event.getStreamId(),
                request.getReason()
        );

        VoiceGatewayResponseDto response =
                voiceGatewayService.stopStream(
                        request
                );

        sendResponse(
                session,
                response
        );

        closeSession(
                session
        );
    }

    // =========================================================
    // CALL RESOLUTION
    // =========================================================

    /**
     * Resolves the internal application Call public ID from
     * the provider call ID.
     *
     * @param event normalized provider event
     * @return application Call public ID
     */
    private String resolveCallId(
            VoiceGatewayEventRequestDto event) {

        if (event == null) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_EVENT
            );
        }

        String providerCallId =
                event.getProviderCallId();

        if (providerCallId == null
                || providerCallId.isBlank()) {

            log.warn(
                    "Provider call ID is missing. " +
                            "streamId={}",
                    event.getStreamId()
            );

            /*
             * Do not introduce a new message constant here.
             * The existing generic Call ID validation message
             * is used according to the project coding standard.
             */
            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }

        return callResolverService.resolveCallId(
                providerCallId
        );
    }

    // =========================================================
    // JSON
    // =========================================================

    /**
     * Parses a raw JSON payload.
     *
     * @param payload raw JSON
     * @return parsed JSON
     */
    private JsonNode readJson(
            String payload) {

        if (payload == null
                || payload.isBlank()) {

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_EVENT
            );
        }

        try {

            return objectMapper.readTree(
                    payload
            );

        } catch (IOException exception) {

            log.error(
                    "Unable to parse Voice Gateway JSON payload.",
                    exception
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.INVALID_EVENT,
                    exception
            );
        }
    }

    /**
     * Reads a text JSON property.
     *
     * @param node JSON node
     * @param fieldName field name
     * @return text value
     */
    private String textValue(
            JsonNode node,
            String fieldName) {

        if (node == null
                || fieldName == null) {

            return null;
        }

        JsonNode value =
                node.get(
                        fieldName
                );

        if (value == null
                || value.isNull()) {

            return null;
        }

        return value.asText();
    }

    /**
     * Reads a numeric JSON property.
     *
     * @param node JSON node
     * @param fieldName field name
     * @return numeric value
     */
    private Long longValue(
            JsonNode node,
            String fieldName) {

        if (node == null
                || fieldName == null) {

            return null;
        }

        JsonNode value =
                node.get(
                        fieldName
                );

        if (value == null
                || value.isNull()
                || !value.isNumber()) {

            return null;
        }

        return value.asLong();
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    /**
     * Sends a gateway response to the provider.
     *
     * @param session WebSocket session
     * @param response gateway response
     */
    private void sendResponse(
            WebSocketSession session,
            VoiceGatewayResponseDto response) {

        if (response == null) {

            log.debug(
                    "No Voice Gateway response to send. " +
                            "sessionId={}",
                    session.getId()
            );

            return;
        }

        if (!session.isOpen()) {

            log.warn(
                    "Cannot send Voice Gateway response because " +
                            "WebSocket session is closed. " +
                            "sessionId={}",
                    session.getId()
            );

            return;
        }

        try {

            String json =
                    objectMapper.writeValueAsString(
                            response
                    );

            session.sendMessage(
                    new TextMessage(
                            json
                    )
            );

            log.debug(
                    "Voice Gateway response sent. " +
                            "sessionId={}, action={}",
                    session.getId(),
                    response.getAction()
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to send Voice Gateway response. " +
                            "sessionId={}",
                    session.getId(),
                    exception
            );
        }
    }

    /**
     * Sends an error response to the provider.
     *
     * @param session WebSocket session
     * @param message error message
     */
    private void sendError(
            WebSocketSession session,
            String message) {

        if (session == null
                || !session.isOpen()) {

            return;
        }

        VoiceGatewayResponseDto response =
                VoiceGatewayResponseDto.builder()
                        .action(
                                VoiceGatewayConstants.ACTION_END
                        )
                        .endCall(
                                true
                        )
                        .responseText(
                                message
                        )
                        .build();

        sendResponse(
                session,
                response
        );
    }

    /**
     * Closes a WebSocket session normally.
     *
     * @param session WebSocket session
     */
    private void closeSession(
            WebSocketSession session) {

        if (session == null
                || !session.isOpen()) {

            return;
        }

        try {

            session.close(
                    CloseStatus.NORMAL
            );

            log.debug(
                    "Voice Gateway WebSocket session closed. " +
                            "sessionId={}",
                    session.getId()
            );

        } catch (IOException exception) {

            log.warn(
                    "Unable to close Voice Gateway WebSocket session. " +
                            "sessionId={}",
                    session.getId(),
                    exception
            );
        }
    }
}