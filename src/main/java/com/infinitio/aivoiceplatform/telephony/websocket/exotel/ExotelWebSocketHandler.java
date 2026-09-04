package com.infinitio.aivoiceplatform.telephony.websocket.exotel;

import java.net.URI;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.telephony.constants.ExotelWebSocketConstants;
import com.infinitio.aivoiceplatform.telephony.dto.websocket.ExotelWebSocketMessage;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayDtmfRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayMediaRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStartRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.dto.request.VoiceGatewayStopRequestDto;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayService;
import com.infinitio.aivoiceplatform.voicegateway.websocket.VoiceGatewayWebSocketSessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles bidirectional Exotel WebSocket media streams.
 *
 * <p>
 * The handler is responsible for the Exotel WebSocket transport
 * lifecycle and provider-specific message parsing. Runtime processing
 * is delegated to the Voice Gateway.
 * </p>
 *
 * <p>
 * The handler does not directly invoke STT, LLM, TTS or Flow services.
 * The Voice Gateway delegates those responsibilities to the appropriate
 * runtime layers.
 * </p>
 *
 * <p>
 * One WebSocket session represents one active Exotel media stream.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExotelWebSocketHandler extends TextWebSocketHandler {

    /**
     * JSON mapper used to parse Exotel messages.
     */
    private final ObjectMapper objectMapper;

    /**
     * Voice Gateway runtime service.
     */
    private final VoiceGatewayService voiceGatewayService;

    /**
     * Registry containing active provider WebSocket sessions.
     */
    private final VoiceGatewayWebSocketSessionRegistry
            webSocketSessionRegistry;

    /**
     * WebSocket session attribute containing the application call ID.
     */
    private static final String SESSION_CALL_ID =
            "exotel.callId";

    /**
     * WebSocket session attribute containing the Exotel call SID.
     */
    private static final String SESSION_CALL_SID =
            "exotel.callSid";

    /**
     * WebSocket session attribute containing the Exotel stream SID.
     */
    private static final String SESSION_STREAM_SID =
            "exotel.streamSid";

    /**
     * WebSocket session attribute containing the audio sample rate.
     */
    private static final String SESSION_SAMPLE_RATE =
            "exotel.sampleRate";

    /**
     * WebSocket session attribute containing the audio encoding.
     */
    private static final String SESSION_AUDIO_ENCODING =
            "exotel.audioEncoding";

    /**
     * WebSocket session attribute containing the channel count.
     */
    private static final String SESSION_CHANNELS =
            "exotel.channels";

    /**
     * Handles a newly established WebSocket connection.
     *
     * @param session WebSocket session
     */
    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        log.info(
                "Exotel WebSocket connection established. " +
                        "sessionId={}, uri={}",
                session.getId(),
                session.getUri()
        );
    }

    /**
     * Processes an incoming Exotel WebSocket message.
     *
     * @param session WebSocket session
     * @param message incoming text message
     */
    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) {

        if (message == null
                || message.getPayload() == null
                || message.getPayload().isBlank()) {

            log.warn(
                    "Ignoring empty Exotel WebSocket message. " +
                            "sessionId={}",
                    session.getId()
            );

            return;
        }

        String payload =
                message.getPayload();

        try {

            JsonNode root =
                    objectMapper.readTree(
                            payload
                    );

            String event =
                    root.path(
                            ExotelWebSocketConstants.FIELD_EVENT
                    ).asText();

            if (event == null
                    || event.isBlank()) {

                log.warn(
                        "Exotel WebSocket message does not contain an " +
                                "event. sessionId={}",
                        session.getId()
                );

                return;
            }

            log.debug(
                    "Exotel WebSocket event received. " +
                            "sessionId={}, event={}",
                    session.getId(),
                    event
            );

            switch (event) {

                case ExotelWebSocketConstants.EVENT_CONNECTED ->

                        handleConnected(
                                session,
                                root
                        );

                case ExotelWebSocketConstants.EVENT_START ->

                        handleStart(
                                session,
                                root
                        );

                case ExotelWebSocketConstants.EVENT_MEDIA ->

                        handleMedia(
                                session,
                                root
                        );

                case ExotelWebSocketConstants.EVENT_DTMF ->

                        handleDtmf(
                                session,
                                root
                        );

                case ExotelWebSocketConstants.EVENT_STOP ->

                        handleStop(
                                session,
                                root
                        );

                case ExotelWebSocketConstants.EVENT_MARK ->

                        handleMark(
                                session,
                                root
                        );

                default ->

                        log.warn(
                                "Unsupported Exotel WebSocket event. " +
                                        "sessionId={}, event={}",
                                session.getId(),
                                event
                        );
            }

        } catch (Exception exception) {

            log.error(
                    "Unable to process Exotel WebSocket message. " +
                            "sessionId={}",
                    session.getId(),
                    exception
            );
        }
    }

    /**
     * Handles the Exotel connection event.
     *
     * @param session WebSocket session
     * @param root parsed message
     */
    private void handleConnected(
            WebSocketSession session,
            JsonNode root) {

        log.info(
                "Exotel WebSocket stream connected. sessionId={}",
                session.getId()
        );
    }

    /**
     * Handles the Exotel start event.
     *
     * <p>
     * The application Call ID and provider stream information are
     * stored in the WebSocket session. The session is then registered
     * with the Voice Gateway and the runtime conversation is started.
     * </p>
     *
     * @param session WebSocket session
     * @param root parsed start message
     */
    private void handleStart(
            WebSocketSession session,
            JsonNode root) {

        ExotelWebSocketMessage message =
                objectMapper.convertValue(
                        root,
                        ExotelWebSocketMessage.class
                );

        String streamSid =
                resolveStreamSid(
                        message,
                        root
                );

        String callSid =
                resolveCallSid(
                        message
                );

        String callId =
                resolveApplicationCallId(
                        session,
                        message
                );

        if (callId == null
                || callId.isBlank()) {

            log.error(
                    "Unable to resolve application Call ID from Exotel " +
                            "start event. sessionId={}, callSid={}, streamSid={}",
                    session.getId(),
                    callSid,
                    streamSid
            );

            throw new IllegalStateException(
                    "Application Call ID could not be resolved from Exotel start event."
            );
        }

        if (streamSid == null
                || streamSid.isBlank()) {

            log.error(
                    "Unable to resolve Exotel stream SID. " +
                            "sessionId={}, callId={}, callSid={}",
                    session.getId(),
                    callId,
                    callSid
            );

            throw new IllegalStateException(
                    "Exotel stream SID could not be resolved."
            );
        }

        session.getAttributes().put(
                SESSION_STREAM_SID,
                streamSid
        );

        session.getAttributes().put(
                SESSION_CALL_SID,
                callSid
        );

        session.getAttributes().put(
                SESSION_CALL_ID,
                callId
        );

        ExotelWebSocketMessage.ExotelMediaFormat mediaFormat =
                message.getStart() != null
                        ? message.getStart().getMediaFormat()
                        : null;

        if (mediaFormat != null) {

            if (mediaFormat.getSampleRate() != null) {

                session.getAttributes().put(
                        SESSION_SAMPLE_RATE,
                        mediaFormat.getSampleRate()
                );
            }

            if (mediaFormat.getEncoding() != null
                    && !mediaFormat.getEncoding().isBlank()) {

                session.getAttributes().put(
                        SESSION_AUDIO_ENCODING,
                        mediaFormat.getEncoding()
                );
            }

            if (mediaFormat.getBitRate() != null) {

                log.debug(
                        "Exotel media format received. " +
                                "sessionId={}, encoding={}, " +
                                "sampleRate={}, bitRate={}",
                        session.getId(),
                        mediaFormat.getEncoding(),
                        mediaFormat.getSampleRate(),
                        mediaFormat.getBitRate()
                );
            }
        }

        session.getAttributes().put(
                SESSION_CHANNELS,
                ExotelWebSocketConstants.DEFAULT_CHANNELS
        );

        /*
         * Register the provider WebSocket before starting the
         * conversation. This allows asynchronous STT/TTS responses
         * to be sent back through the same Exotel connection.
         */
        webSocketSessionRegistry.register(
                callId,
                session
        );

        VoiceGatewayStartRequestDto startRequest =
                VoiceGatewayStartRequestDto.builder()
                        .callId(
                                callId
                        )
                        .providerCallId(
                                callSid
                        )
                        .streamId(
                                streamSid
                        )
                        .providerAccountId(
                                message.getStart() != null
                                        ? message.getStart().getAccountSid()
                                        : null
                        )
                        .audioEncoding(
                                getStringAttribute(
                                        session,
                                        SESSION_AUDIO_ENCODING
                                )
                        )
                        .sampleRate(
                                getIntegerAttribute(
                                        session,
                                        SESSION_SAMPLE_RATE
                                )
                        )
                        .channels(
                                getIntegerAttribute(
                                        session,
                                        SESSION_CHANNELS
                                )
                        )
                        .sampleSizeBits(
                                ExotelWebSocketConstants
                                        .AUDIO_SAMPLE_SIZE_BITS
                        )
                        .callerNumber(
                                message.getStart() != null
                                        ? message.getStart().getFrom()
                                        : null
                        )
                        .calledNumber(
                                message.getStart() != null
                                        ? message.getStart().getTo()
                                        : null
                        )
                        .metadata(
                                message.getStart() != null
                                        ? message.getStart().getCustomParameters()
                                        : null
                        )
                        .build();

        voiceGatewayService.startStream(
                startRequest
        );

        log.info(
                "Exotel WebSocket stream started and Voice Gateway " +
                        "runtime initialized. sessionId={}, callId={}, " +
                        "callSid={}, streamSid={}, sampleRate={}, encoding={}",
                session.getId(),
                callId,
                callSid,
                streamSid,
                session.getAttributes().get(
                        SESSION_SAMPLE_RATE
                ),
                session.getAttributes().get(
                        SESSION_AUDIO_ENCODING
                )
        );
    }

    /**
     * Handles an incoming Exotel media event.
     *
     * <p>
     * The Base64 audio payload is decoded and forwarded to the
     * Voice Gateway. The Voice Gateway streams the decoded audio
     * to the active STT runtime.
     * </p>
     *
     * @param session WebSocket session
     * @param root parsed media message
     */
    /**
     * Handles an incoming Exotel media event.
     *
     * <p>
     * The Base64 audio payload is decoded and forwarded to the
     * Voice Gateway. The Voice Gateway streams the decoded audio
     * to the active STT runtime.
     * </p>
     *
     * @param session WebSocket session
     * @param root parsed media message
     */
    private void handleMedia(
            WebSocketSession session,
            JsonNode root) {

        JsonNode mediaNode =
                root.path(
                        ExotelWebSocketConstants.FIELD_MEDIA
                );

        String payload =
                mediaNode.path(
                        ExotelWebSocketConstants.FIELD_PAYLOAD
                ).asText();

        if (payload == null
                || payload.isBlank()) {

            log.warn(
                    "Exotel media event contains no audio payload. " +
                            "sessionId={}, callId={}",
                    session.getId(),
                    getCallId(session)
            );

            return;
        }

        byte[] audio;

        try {

            audio =
                    Base64.getDecoder()
                            .decode(
                                    payload
                            );

        } catch (IllegalArgumentException exception) {

            log.error(
                    "Invalid Base64 audio received from Exotel. " +
                            "sessionId={}, callId={}",
                    session.getId(),
                    getCallId(session),
                    exception
            );

            return;
        }

        if (audio.length == 0) {

            log.debug(
                    "Ignoring empty Exotel audio packet. " +
                            "sessionId={}, callId={}",
                    session.getId(),
                    getCallId(session)
            );

            return;
        }

        String streamSid =
                root.path(
                        ExotelWebSocketConstants.FIELD_STREAM_SID
                ).asText();

        if (streamSid == null
                || streamSid.isBlank()) {

            streamSid =
                    getStreamSid(
                            session
                    );
        }

        String callId =
                getCallId(
                        session
                );

        if (callId == null
                || callId.isBlank()) {

            log.error(
                    "Cannot forward Exotel media because application " +
                            "Call ID is missing. sessionId={}",
                    session.getId()
            );

            return;
        }

        Long sequenceNumber =
                getLongValue(
                        root,
                        ExotelWebSocketConstants.FIELD_SEQUENCE_NUMBER
                );

        Long chunk =
                getLongValue(
                        mediaNode,
                        ExotelWebSocketConstants.FIELD_CHUNK
                );

        Long timestamp =
                getLongValue(
                        mediaNode,
                        ExotelWebSocketConstants.FIELD_TIMESTAMP
                );

        Integer sampleRate =
                getIntegerAttribute(
                        session,
                        SESSION_SAMPLE_RATE
                );

        String encoding =
                getStringAttribute(
                        session,
                        SESSION_AUDIO_ENCODING
                );

        Integer channels =
                getIntegerAttribute(
                        session,
                        SESSION_CHANNELS
                );

        log.debug(
                "Exotel audio packet received. " +
                        "sessionId={}, callId={}, streamSid={}, " +
                        "sequenceNumber={}, chunk={}, timestamp={}, " +
                        "audioBytes={}, sampleRate={}, encoding={}, channels={}",
                session.getId(),
                callId,
                streamSid,
                sequenceNumber,
                chunk,
                timestamp,
                audio.length,
                sampleRate,
                encoding,
                channels
        );

        VoiceGatewayMediaRequestDto mediaRequest =
                VoiceGatewayMediaRequestDto.builder()
                        .callId(
                                callId
                        )
                        .providerCallId(
                                getCallSid(session)
                        )
                        .streamId(
                                streamSid
                        )
                        .sequenceNumber(
                                sequenceNumber
                        )
                        .chunk(
                                chunk
                        )
                        .timestamp(
                                timestamp
                        )
                        .audioBase64(
                                payload
                        )
                        .audioEncoding(
                                encoding
                        )
                        .sampleRate(
                                sampleRate
                        )
                        .channels(
                                channels
                        )
                        .sampleSizeBits(
                                ExotelWebSocketConstants
                                        .AUDIO_SAMPLE_SIZE_BITS
                        )
                        .inbound(
                                true
                        )
                        .valid(
                                true
                        )
                        .build();

        voiceGatewayService.processMedia(
                mediaRequest
        );
    }

    /**
     * Handles an Exotel DTMF event.
     *
     * <p>
     * DTMF input is forwarded to the Voice Gateway. The Voice
     * Gateway remains responsible for deciding how the runtime
     * should process the digit.
     * </p>
     *
     * @param session WebSocket session
     * @param root parsed DTMF message
     */
    private void handleDtmf(
            WebSocketSession session,
            JsonNode root) {

        JsonNode dtmfNode =
                root.path(
                        ExotelWebSocketConstants.FIELD_DTMF
                );

        String digit =
                dtmfNode.path(
                        ExotelWebSocketConstants.FIELD_DIGIT
                ).asText();

        String duration =
                dtmfNode.path(
                        ExotelWebSocketConstants.FIELD_DURATION
                ).asText();

        if (digit == null
                || digit.isBlank()) {

            log.warn(
                    "Exotel DTMF event contains no digit. " +
                            "sessionId={}, callId={}",
                    session.getId(),
                    getCallId(session)
            );

            return;
        }

        String callId =
                getCallId(
                        session
                );

        String streamSid =
                getStreamSid(
                        session
                );

        log.info(
                "Exotel DTMF received. " +
                        "sessionId={}, callId={}, streamSid={}, " +
                        "digit={}, duration={}",
                session.getId(),
                callId,
                streamSid,
                digit,
                duration
        );

        VoiceGatewayDtmfRequestDto dtmfRequest =
                VoiceGatewayDtmfRequestDto.builder()
                        .callId(
                                callId
                        )
                        .streamId(
                                streamSid
                        )
                        .digit(
                                digit
                        )
                        .build();

        voiceGatewayService.processDtmf(
                dtmfRequest
        );
    }

    /**
     * Handles an Exotel stop event.
     *
     * @param session WebSocket session
     * @param root parsed stop message
     */
    private void handleStop(
            WebSocketSession session,
            JsonNode root) {

        JsonNode stopNode =
                root.path(
                        ExotelWebSocketConstants.FIELD_STOP
                );

        String reason =
                stopNode.path(
                        ExotelWebSocketConstants.FIELD_REASON
                ).asText();

        String callId =
                getCallId(
                        session
                );

        String callSid =
                getCallSid(
                        session
                );

        String streamSid =
                getStreamSid(
                        session
                );

        log.info(
                "Exotel WebSocket stream stopped. " +
                        "sessionId={}, callId={}, callSid={}, " +
                        "streamSid={}, reason={}",
                session.getId(),
                callId,
                callSid,
                streamSid,
                reason
        );

        if (callId == null
                || callId.isBlank()
                || streamSid == null
                || streamSid.isBlank()) {

            log.warn(
                    "Unable to notify Voice Gateway about Exotel " +
                            "stream stop because runtime identifiers " +
                            "are missing. sessionId={}, callId={}, streamSid={}",
                    session.getId(),
                    callId,
                    streamSid
            );

            return;
        }

        VoiceGatewayStopRequestDto stopRequest =
                VoiceGatewayStopRequestDto.builder()
                        .callId(
                                callId
                        )
                        .providerCallId(
                                callSid
                        )
                        .streamId(
                                streamSid
                        )
                        .reason(
                                reason
                        )
                        .build();

        voiceGatewayService.stopStream(
                stopRequest
        );
    }

    /**
     * Handles a provider mark event.
     *
     * @param session WebSocket session
     * @param root parsed mark message
     */
    private void handleMark(
            WebSocketSession session,
            JsonNode root) {

        JsonNode markNode =
                root.path(
                        ExotelWebSocketConstants.FIELD_MARK
                );

        String name =
                markNode.path(
                        ExotelWebSocketConstants.FIELD_NAME
                ).asText();

        log.debug(
                "Exotel mark received. " +
                        "sessionId={}, callId={}, streamSid={}, name={}",
                session.getId(),
                getCallId(session),
                getStreamSid(session),
                name
        );
    }

    /**
     * Handles WebSocket connection closure.
     *
     * @param session WebSocket session
     * @param status close status
     */
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        String callId =
                getCallId(
                        session
                );

        log.info(
                "Exotel WebSocket connection closed. " +
                        "sessionId={}, callId={}, callSid={}, " +
                        "streamSid={}, status={}",
                session.getId(),
                callId,
                getCallSid(session),
                getStreamSid(session),
                status
        );

        /*
         * The normal Exotel STOP event should already have stopped
         * the Voice Gateway runtime. This removal is intentionally
         * defensive for abnormal WebSocket termination.
         */
        if (callId != null
                && !callId.isBlank()) {

            webSocketSessionRegistry.remove(
                    callId
            );
        }
    }

    /**
     * Handles transport errors.
     *
     * @param session WebSocket session
     * @param exception transport exception
     */
    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception) {

        log.error(
                "Exotel WebSocket transport error. " +
                        "sessionId={}, callId={}, callSid={}, " +
                        "streamSid={}",
                session.getId(),
                getCallId(session),
                getCallSid(session),
                getStreamSid(session),
                exception
        );
    }

    /**
     * Resolves the Exotel stream identifier.
     *
     * @param message parsed Exotel message
     * @param root raw JSON
     * @return stream identifier
     */
    private String resolveStreamSid(
            ExotelWebSocketMessage message,
            JsonNode root) {

        if (message.getStreamSid() != null
                && !message.getStreamSid().isBlank()) {

            return message.getStreamSid();
        }

        if (message.getStart() != null
                && message.getStart().getStreamSid() != null
                && !message.getStart().getStreamSid().isBlank()) {

            return message
                    .getStart()
                    .getStreamSid();
        }

        return root.path(
                ExotelWebSocketConstants.FIELD_STREAM_SID
        ).asText();
    }

    /**
     * Resolves the Exotel call identifier.
     *
     * @param message parsed Exotel message
     * @return Exotel call identifier
     */
    private String resolveCallSid(
            ExotelWebSocketMessage message) {

        if (message.getStart() == null) {

            return null;
        }

        return message
                .getStart()
                .getCallSid();
    }

    /**
     * Resolves the application call identifier.
     *
     * <p>
     * The application call identifier is first resolved from Exotel
     * custom parameters. When no custom parameter is available, the
     * provider call SID is retained as the fallback transport
     * identifier.
     * </p>
     *
     * @param session WebSocket session
     * @param message parsed Exotel message
     * @return application call identifier
     */
    private String resolveApplicationCallId(
            WebSocketSession session,
            ExotelWebSocketMessage message) {

        String callId =
                resolveCallIdFromCustomParameters(
                        message
                );

        if (callId != null
                && !callId.isBlank()) {

            return callId;
        }

        if (message.getStart() != null
                && message.getStart().getCallSid() != null
                && !message.getStart().getCallSid().isBlank()) {

            return message
                    .getStart()
                    .getCallSid();
        }

        return resolveCallIdFromUri(
                session.getUri()
        );
    }

    /**
     * Resolves an application call identifier from Exotel
     * custom parameters.
     *
     * @param message parsed Exotel message
     * @return call identifier or null
     */
    private String resolveCallIdFromCustomParameters(
            ExotelWebSocketMessage message) {

        if (message.getStart() == null
                || message.getStart().getCustomParameters() == null) {

            return null;
        }

        Object customParameters =
                message
                        .getStart()
                        .getCustomParameters();

        if (!(customParameters instanceof Map<?, ?> parameters)) {

            return null;
        }

        Object callId =
                parameters.get(
                        "call_id"
                );

        if (callId == null) {

            callId =
                    parameters.get(
                            "callId"
                    );
        }

        return callId != null
                ? callId.toString()
                : null;
    }

    /**
     * Resolves a call identifier from the WebSocket URI.
     *
     * @param uri WebSocket URI
     * @return call identifier or null
     */
    private String resolveCallIdFromUri(
            URI uri) {

        if (uri == null
                || uri.getQuery() == null
                || uri.getQuery().isBlank()) {

            return null;
        }

        String[] parameters =
                uri.getQuery().split(
                        "&"
                );

        for (String parameter : parameters) {

            String[] pair =
                    parameter.split(
                            "=",
                            2
                    );

            if (pair.length != 2) {

                continue;
            }

            if ("callPublicId".equalsIgnoreCase(pair[0])
                    || "callId".equalsIgnoreCase(pair[0])) {

                return pair[1];
            }
        }

        return null;
    }

    /**
     * Returns the application call identifier from the session.
     *
     * @param session WebSocket session
     * @return call identifier
     */
    private String getCallId(
            WebSocketSession session) {

        return getStringAttribute(
                session,
                SESSION_CALL_ID
        );
    }

    /**
     * Returns the Exotel call SID from the session.
     *
     * @param session WebSocket session
     * @return call SID
     */
    private String getCallSid(
            WebSocketSession session) {

        return getStringAttribute(
                session,
                SESSION_CALL_SID
        );
    }

    /**
     * Returns the Exotel stream SID from the session.
     *
     * @param session WebSocket session
     * @return stream SID
     */
    private String getStreamSid(
            WebSocketSession session) {

        return getStringAttribute(
                session,
                SESSION_STREAM_SID
        );
    }

    /**
     * Returns a String WebSocket session attribute.
     *
     * @param session WebSocket session
     * @param attributeName attribute name
     * @return attribute value or null
     */
    private String getStringAttribute(
            WebSocketSession session,
            String attributeName) {

        Object value =
                session
                        .getAttributes()
                        .get(
                                attributeName
                        );

        return value != null
                ? value.toString()
                : null;
    }

    /**
     * Returns an Integer WebSocket session attribute.
     *
     * @param session WebSocket session
     * @param attributeName attribute name
     * @return attribute value or null
     */
    private Integer getIntegerAttribute(
            WebSocketSession session,
            String attributeName) {

        Object value =
                session
                        .getAttributes()
                        .get(
                                attributeName
                        );

        if (value instanceof Integer integerValue) {

            return integerValue;
        }

        if (value instanceof Number numberValue) {

            return numberValue.intValue();
        }

        if (value != null) {

            try {

                return Integer.valueOf(
                        value.toString()
                );

            } catch (NumberFormatException exception) {

                log.debug(
                        "Unable to parse Exotel WebSocket integer " +
                                "session attribute. attribute={}, value={}",
                        attributeName,
                        value
                );
            }
        }

        return null;
    }

    /**
     * Reads a nullable long value from a JSON node.
     *
     * <p>
     * Exotel may omit sequence, chunk or timestamp values in
     * malformed or incomplete events. In that case this method
     * returns {@code null} instead of converting the missing
     * value to zero.
     * </p>
     *
     * @param node JSON node
     * @param fieldName field name
     * @return parsed long value or null
     */
    private Long getLongValue(
            JsonNode node,
            String fieldName) {

        if (node == null
                || fieldName == null
                || !node.hasNonNull(fieldName)) {

            return null;
        }

        JsonNode value =
                node.path(
                        fieldName
                );

        if (value.isIntegralNumber()) {

            return value.longValue();
        }

        String text =
                value.asText();

        if (text == null
                || text.isBlank()) {

            return null;
        }

        try {

            return Long.valueOf(
                    text
            );

        } catch (NumberFormatException exception) {

            log.warn(
                    "Invalid numeric Exotel WebSocket field. " +
                            "field={}, value={}",
                    fieldName,
                    text
            );

            return null;
        }
    }
}