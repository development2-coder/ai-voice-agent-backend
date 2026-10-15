package com.infinitio.aivoiceplatform.telephony.inbound.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.telephony.constants.ExotelWebSocketConstants;
import com.infinitio.aivoiceplatform.telephony.dto.websocket.ExotelWebSocketMessage;
import com.infinitio.aivoiceplatform.telephony.inbound.constants.InboundTelephonyConstants;
import com.infinitio.aivoiceplatform.telephony.inbound.service.InboundCallPreparationService;
import com.infinitio.aivoiceplatform.telephony.inbound.service.InboundCallStatusService;
import com.infinitio.aivoiceplatform.telephony.websocket.exotel.ExotelWebSocketHandler;
import com.infinitio.aivoiceplatform.telephony.inbound.config.InboundTelephonyProperties;
import com.infinitio.aivoiceplatform.call.entity.Call;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket handler for inbound Exotel voice streams.
 *
 * <p>
 * This handler prepares the application Call and CallSession
 * when the Exotel START event is received and then delegates
 * the complete media processing lifecycle to the existing
 * Exotel WebSocket handler.
 * </p>
 *
 * <p>
 * Existing STT, TTS, Voice Gateway, recording, transcript,
 * DTMF and media processing are intentionally not duplicated.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InboundExotelWebSocketHandler
        extends TextWebSocketHandler {

    /**
     * JSON mapper used to parse Exotel WebSocket messages.
     */
    private final ObjectMapper objectMapper;

    /**
     * Service responsible for preparing the inbound Call
     * and CallSession.
     */
    private final InboundCallPreparationService
            inboundCallPreparationService;

    /**
     * Existing Exotel WebSocket handler responsible for
     * media and Voice Gateway processing.
     */
    private final ExotelWebSocketHandler
            exotelWebSocketHandler;

    private final InboundTelephonyProperties
            inboundTelephonyProperties;

    private final InboundCallStatusService inboundCallStatusService;

    /**
     * Handles a newly established inbound WebSocket connection.
     *
     * @param session WebSocket session
     */
    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        log.info(
                "Inbound Exotel WebSocket connection established. " +
                        "sessionId={}, uri={}",
                session.getId(),
                session.getUri()
        );

        /*
         * Delegate lifecycle handling to the existing handler.
         */
        exotelWebSocketHandler.afterConnectionEstablished(
                session
        );
    }

    /**
     * Delegates a WebSocket message to the existing Exotel
     * message-processing pipeline.
     *
     * <p>
     * This method is exposed for provider-specific WebSocket
     * adapters such as inbound telephony, while keeping the
     * existing message-processing implementation unchanged.
     * </p>
     *
     * @param session WebSocket session
     * @param message incoming WebSocket message
     */
    public void handleMessage(
            WebSocketSession session,
            TextMessage message) {

        handleTextMessage(
                session,
                message
        );
    }

    /**
     * Processes an inbound Exotel WebSocket message.
     *
     * <p>
     * The inbound Call is prepared before the existing handler
     * processes the START event. This is required because the
     * existing handler resolves the application Call ID using
     * the Exotel provider call SID.
     * </p>
     *
     * @param session WebSocket session
     * @param message incoming WebSocket message
     */
    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) {

        if (message == null
                || message.getPayload() == null
                || message.getPayload().isBlank()) {

            log.warn(
                    "Ignoring empty inbound Exotel WebSocket message. " +
                            "sessionId={}",
                    session.getId()
            );

            return;
        }

        String payload =
                message.getPayload();

        try {

            JsonNode rootNode =
                    objectMapper.readTree(
                            payload
                    );

            String event =
                    rootNode.path(
                            ExotelWebSocketConstants.FIELD_EVENT
                    ).asText();

            if (event == null
                    || event.isBlank()) {

                log.warn(
                        "Inbound Exotel WebSocket message does not " +
                                "contain an event. sessionId={}",
                        session.getId()
                );

                return;
            }

            log.debug(
                    "Inbound Exotel WebSocket event received. " +
                            "sessionId={}, event={}",
                    session.getId(),
                    event
            );

            /*
             * -----------------------------------------------------
             * START EVENT
             * -----------------------------------------------------
             *
             * Prepare the application Call and CallSession BEFORE
             * the existing handler processes the START event.
             */
            if (ExotelWebSocketConstants.EVENT_START
                    .equalsIgnoreCase(event)) {

                prepareInboundCall(
                        session,
                        rootNode
                );
            }

            if (ExotelWebSocketConstants.EVENT_MEDIA
                    .equalsIgnoreCase(event)) {

                markInboundCallInProgress(session);
            }

            if (ExotelWebSocketConstants.EVENT_STOP
                    .equalsIgnoreCase(event)) {

                markInboundCallCompleted(session);
            }

            /*
             * -----------------------------------------------------
             * EXISTING EXOTEL RUNTIME
             * -----------------------------------------------------
             *
             * Do not duplicate existing media/STT/TTS/runtime
             * implementation. The current working handler remains
             * the single owner of Exotel media processing.
             */
            exotelWebSocketHandler.handleMessage(
                    session,
                    message
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to process inbound Exotel WebSocket " +
                            "message. sessionId={}, event={}",
                    session.getId(),
                    extractEvent(payload),
                    exception
            );

            markInboundCallFailed(session);

            closeSessionAfterPreparationFailure(
                    session
            );
        }
    }

    /**
     * Marks the inbound Call as failed when processing
     * of the WebSocket event fails.
     *
     * @param session inbound WebSocket session
     */
    private void markInboundCallFailed(
            WebSocketSession session) {

        Call call = getInboundCall(session);

        if (call == null) {
            return;
        }

        inboundCallStatusService.markFailed(call);
    }

    /**
     * Marks the inbound Call as in progress when media
     * processing starts.
     *
     * @param session inbound WebSocket session
     */
    private void markInboundCallInProgress(
            WebSocketSession session) {

        Call call = getInboundCall(session);

        if (call == null) {
            return;
        }

        inboundCallStatusService.markInProgress(call);
    }

    /**
     * Marks the inbound Call as completed when Exotel
     * sends the STOP event.
     *
     * @param session inbound WebSocket session
     */
    private void markInboundCallCompleted(
            WebSocketSession session) {

        Call call = getInboundCall(session);

        if (call == null) {
            return;
        }

        inboundCallStatusService.markCompleted(call);
    }

    /**
     * Prepares the application Call and CallSession from
     * the Exotel START event.
     *
     * @param session WebSocket session
     * @param rootNode parsed Exotel START event
     */
    private void prepareInboundCall(
            WebSocketSession session,
            JsonNode rootNode) {

        validateInboundConfiguration();

        ExotelWebSocketMessage message =
                objectMapper.convertValue(
                        rootNode,
                        ExotelWebSocketMessage.class
                );

        ExotelWebSocketMessage.ExotelStartMessage start =
                message.getStart();

        if (start == null) {

            throw new IllegalStateException(
                    "Exotel START event does not contain start metadata."
            );
        }

        String providerCallId =
                start.getCallSid();

        String callerNumber =
                start.getFrom();

        String calledNumber =
                start.getTo();

        validateStartData(
                providerCallId,
                callerNumber,
                calledNumber
        );

        log.info(
                "Preparing inbound Exotel call. " +
                        "sessionId={}, providerCallId={}, " +
                        "callerNumber={}, calledNumber={}",
                session.getId(),
                providerCallId,
                maskPhoneNumber(callerNumber),
                maskPhoneNumber(calledNumber)
        );

        Call call =
                inboundCallPreparationService
                        .prepareInboundCall(
                                inboundTelephonyProperties
                                        .getProvider(),
                                providerCallId,
                                callerNumber,
                                calledNumber
                        );

        session.getAttributes().put(
                InboundTelephonyConstants.ATTRIBUTE_CALL,
                call
        );

        /*
         * Store inbound information in the WebSocket session.
         *
         * The existing ExotelWebSocketHandler will overwrite its
         * own standard attributes when it processes START.
         */
        session.getAttributes().put(
                InboundTelephonyConstants.SESSION_CALL_ID,
                call.getPublicId()
        );

        session.getAttributes().put(
                InboundTelephonyConstants
                        .SESSION_PROVIDER_CALL_ID,
                providerCallId
        );

        session.getAttributes().put(
                InboundTelephonyConstants
                        .SESSION_CALLER_NUMBER,
                callerNumber
        );

        session.getAttributes().put(
                InboundTelephonyConstants
                        .SESSION_CALLED_NUMBER,
                calledNumber
        );

        if (start.getStreamSid() != null
                && !start.getStreamSid().isBlank()) {

            session.getAttributes().put(
                    InboundTelephonyConstants
                            .SESSION_STREAM_ID,
                    start.getStreamSid()
            );
        }

        if (start.getMediaFormat() != null) {

            if (start.getMediaFormat().getSampleRate()
                    != null) {

                session.getAttributes().put(
                        InboundTelephonyConstants
                                .SESSION_SAMPLE_RATE,
                        start.getMediaFormat()
                                .getSampleRate()
                );
            }

            if (start.getMediaFormat().getEncoding()
                    != null
                    && !start.getMediaFormat()
                    .getEncoding()
                    .isBlank()) {

                session.getAttributes().put(
                        InboundTelephonyConstants
                                .SESSION_AUDIO_ENCODING,
                        start.getMediaFormat()
                                .getEncoding()
                );
            }
        }

        log.info(
                "Inbound Exotel Call prepared successfully. " +
                        "sessionId={}, callPublicId={}, " +
                        "providerCallId={}, streamId={}",
                session.getId(),
                call.getPublicId(),
                providerCallId,
                start.getStreamSid()
        );
    }

    /**
     * Validates the inbound telephony configuration required
     * for processing the WebSocket call.
     */
    private void validateInboundConfiguration() {

        if (!Boolean.TRUE.equals(
                inboundTelephonyProperties.getEnabled())) {

            throw new IllegalStateException(
                    "Inbound telephony is disabled."
            );
        }

        if (inboundTelephonyProperties.getProvider() == null
                || inboundTelephonyProperties
                .getProvider()
                .isBlank()) {

            throw new IllegalStateException(
                    "Inbound telephony provider is not configured."
            );
        }
    }

    /**
     * Validates required values received from Exotel START.
     *
     * @param providerCallId provider call ID
     * @param callerNumber caller number
     * @param calledNumber called number
     */
    private void validateStartData(
            String providerCallId,
            String callerNumber,
            String calledNumber) {

        if (providerCallId == null
                || providerCallId.isBlank()) {

            throw new IllegalArgumentException(
                    "Exotel provider call ID is required."
            );
        }

        if (callerNumber == null
                || callerNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Exotel caller number is required."
            );
        }

        if (calledNumber == null
                || calledNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Exotel called number is required."
            );
        }
    }

    /**
     * Closes the WebSocket when inbound call preparation fails.
     *
     * @param session WebSocket session
     */
    private void closeSessionAfterPreparationFailure(
            WebSocketSession session) {

        try {

            if (session.isOpen()) {

                session.close(
                        CloseStatus.SERVER_ERROR
                );
            }

        } catch (Exception closeException) {

            log.error(
                    "Unable to close inbound Exotel WebSocket " +
                            "after preparation failure. sessionId={}",
                    session.getId(),
                    closeException
            );
        }
    }

    /**
     * Extracts an event name for error logging.
     *
     * @param payload WebSocket payload
     * @return event name or UNKNOWN
     */
    private String extractEvent(
            String payload) {

        try {

            JsonNode root =
                    objectMapper.readTree(
                            payload
                    );

            return root.path(
                    ExotelWebSocketConstants.FIELD_EVENT
            ).asText("UNKNOWN");

        } catch (Exception exception) {

            return "UNKNOWN";
        }
    }

    /**
     * Masks phone number values before writing them to logs.
     *
     * @param phoneNumber phone number
     * @return masked phone number
     */
    private String maskPhoneNumber(
            String phoneNumber) {

        if (phoneNumber == null
                || phoneNumber.isBlank()) {

            return "****";
        }

        String normalized =
                phoneNumber.trim();

        if (normalized.length() <= 4) {

            return "****";
        }

        return "****"
                + normalized.substring(
                normalized.length() - 4
        );
    }

    /**
     * Handles inbound WebSocket connection closure.
     *
     * @param session WebSocket session
     * @param status close status
     */
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        log.info(
                "Inbound Exotel WebSocket connection closed. " +
                        "sessionId={}, statusCode={}, reason={}",
                session.getId(),
                status.getCode(),
                status.getReason()
        );

        exotelWebSocketHandler.afterConnectionClosed(
                session,
                status
        );
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
                "Inbound Exotel WebSocket transport error. " +
                        "sessionId={}, message={}",
                session.getId(),
                exception.getMessage(),
                exception
        );

        markInboundCallFailed(session);

        exotelWebSocketHandler.handleTransportError(
                session,
                exception
        );
    }

    /**
     * Retrieves the inbound Call associated with the WebSocket session.
     *
     * @param session inbound WebSocket session
     * @return associated Call, or null when unavailable
     */
    private Call getInboundCall(
            WebSocketSession session) {

        Object value =
                session.getAttributes().get(
                        InboundTelephonyConstants.ATTRIBUTE_CALL
                );

        if (value instanceof Call call) {
            return call;
        }

        log.warn(
                "Inbound Call not found in WebSocket session. " +
                        "sessionId={}",
                session.getId()
        );

        return null;
    }
}