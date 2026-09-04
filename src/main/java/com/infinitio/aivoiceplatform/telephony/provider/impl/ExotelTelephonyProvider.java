package com.infinitio.aivoiceplatform.telephony.provider.impl;

import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.infinitio.aivoiceplatform.telephony.dto.request.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.telephony.config.ExotelProperties;
import com.infinitio.aivoiceplatform.telephony.constants.TelephonyConstants;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NumberResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.provider.TelephonyProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Exotel implementation of the telephony provider.
 *
 * <p>
 * This class is responsible only for Exotel-specific
 * communication and provider-to-platform event normalization.
 * </p>
 *
 * <p>
 * Provider-specific statuses are converted into normalized
 * telephony events before they are returned to the application.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExotelTelephonyProvider implements TelephonyProvider {

    private static final String PROVIDER_CODE =
            TelephonyConstants.PROVIDER_EXOTEL;

    private static final String CONNECT_CALL_PATH =
            "/v1/Accounts/{accountSid}/Calls/connect";

    /*
     * ---------------------------------------------------------
     * EXOTEL CALLBACK FIELDS
     * ---------------------------------------------------------
     */

    private static final String FIELD_FROM = "From";
    private static final String FIELD_CALLER_ID = "CallerId";
    private static final String FIELD_URL = "Url";
    private static final String FIELD_STATUS_CALLBACK = "StatusCallback";
    private static final String FIELD_CALL_SID = "CallSid";
    private static final String FIELD_STATUS = "Status";
    private static final String FIELD_RECORDING_URL = "RecordingUrl";
    private static final String FIELD_DATE_UPDATED = "DateUpdated";
    private static final String FIELD_TO = "To";

    private static final String FIELD_STREAM_URL = "StreamUrl";
    private static final String FIELD_STREAM_TYPE = "StreamType";
    private static final String FIELD_RECORD = "Record";
    private static final String FIELD_RECORDING_CHANNELS =
            "RecordingChannels";
    private static final String FIELD_TIME_LIMIT = "TimeLimit";

    private static final String FIELD_STATUS_CALLBACK_EVENTS =
            "StatusCallbackEvents[]";

    private static final DateTimeFormatter EXOTEL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient exotelRestClient;

    private final ExotelProperties exotelProperties;

    private final ObjectMapper objectMapper;

    /**
     * Returns the provider code.
     *
     * @return provider code
     */
    @Override
    public String getProviderCode() {

        return PROVIDER_CODE;
    }

    /**
     * Places an outbound call through Exotel.
     *
     * <p>
     * Exotel first calls the destination number supplied in
     * the {@code From} field and uses the configured caller ID
     * as the displayed caller number.
     * </p>
     *
     * @param request outbound call request
     * @return provider call response
     */
    @Override
    public ProviderCallResponseDto placeOutboundCall(
            PlaceOutboundCallRequestDto request) {

        validateOutboundCallRequest(request);

        boolean realtime =
                request.getStreamUrl() != null
                        && !request.getStreamUrl().isBlank();

        log.info(
                "Placing outbound Exotel call. from={}, to={}, realtime={}",
                request.getFromNumber(),
                request.getToNumber(),
                realtime
        );

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        /*
         * ---------------------------------------------------------
         * EXOTEL CONNECT API
         * ---------------------------------------------------------
         *
         * From:
         * Number Exotel should call first.
         *
         * CallerId:
         * Exotel number displayed to the destination.
         */

        formData.add(
                FIELD_FROM,
                request.getToNumber()
        );

        formData.add(
                FIELD_CALLER_ID,
                request.getFromNumber()
        );

        if (realtime) {

            addRealtimeConfiguration(
                    formData,
                    request
            );

        } else {

            addApplicationFlowConfiguration(
                    formData
            );
        }

        addCallbackConfiguration(
                formData,
                request
        );

        log.debug(
                "Sending Exotel outbound call request. "
                        + "realtime={}, callbackConfigured={}",
                realtime,
                isCallbackConfigured(request)
        );

        String responseBody;

        try {

            responseBody =
                    exotelRestClient
                            .post()
                            .uri(
                                    CONNECT_CALL_PATH,
                                    exotelProperties.getAccountSid()
                            )
                            .headers(
                                    headers ->
                                            headers.setBasicAuth(
                                                    exotelProperties.getApiKey(),
                                                    exotelProperties.getApiToken()
                                            )
                            )
                            .contentType(
                                    MediaType.APPLICATION_FORM_URLENCODED
                            )
                            .body(formData)
                            .retrieve()
                            .body(String.class);

        } catch (RestClientResponseException exception) {

            String errorBody =
                    exception.getResponseBodyAsString();

            String exotelError =
                    parseExotelErrorResponse(
                            errorBody
                    );

            log.error(
                    "Exotel outbound call request failed. "
                            + "httpStatus={}, error={}",
                    exception.getStatusCode().value(),
                    exotelError
            );

            throw new IllegalStateException(
                    "Exotel outbound call failed. "
                            + exotelError,
                    exception
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to send outbound call request to Exotel.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to send outbound call request to Exotel.",
                    exception
            );
        }

        log.debug(
                "Exotel outbound call response received."
        );

        return buildProviderCallResponse(
                responseBody
        );
    }

    /**
     * Validates the outbound call request.
     *
     * @param request outbound call request
     */
    private void validateOutboundCallRequest(
            PlaceOutboundCallRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Outbound call request is required."
            );
        }

        if (request.getFromNumber() == null
                || request.getFromNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Outbound call source number is required."
            );
        }

        if (request.getToNumber() == null
                || request.getToNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Outbound call destination number is required."
            );
        }

        if (exotelProperties.getAccountSid() == null
                || exotelProperties.getAccountSid().isBlank()) {

            throw new IllegalStateException(
                    "Exotel account SID is not configured."
            );
        }

        if (exotelProperties.getApiKey() == null
                || exotelProperties.getApiKey().isBlank()
                || exotelProperties.getApiToken() == null
                || exotelProperties.getApiToken().isBlank()) {

            throw new IllegalStateException(
                    "Exotel API credentials are not configured."
            );
        }
    }

    /**
     * Adds realtime streaming configuration to the Exotel
     * outbound request.
     *
     * @param formData Exotel form data
     * @param request outbound call request
     */
    private void addRealtimeConfiguration(
            MultiValueMap<String, String> formData,
            PlaceOutboundCallRequestDto request) {

        formData.add(
                FIELD_STREAM_URL,
                request.getStreamUrl()
        );

        String streamType =
                request.getStreamType();

        if (streamType == null
                || streamType.isBlank()) {

            streamType =
                    exotelProperties.getStreamType();
        }

        if (streamType != null
                && !streamType.isBlank()) {

            formData.add(
                    FIELD_STREAM_TYPE,
                    streamType
            );
        }

        Boolean record =
                request.getRecord();

        if (record == null) {

            record =
                    exotelProperties.getRecord();
        }

        if (record != null) {

            formData.add(
                    FIELD_RECORD,
                    String.valueOf(record)
            );
        }

        String recordingChannels =
                request.getRecordingChannels();

        if (recordingChannels == null
                || recordingChannels.isBlank()) {

            recordingChannels =
                    exotelProperties.getRecordingChannels();
        }

        if (recordingChannels != null
                && !recordingChannels.isBlank()) {

            formData.add(
                    FIELD_RECORDING_CHANNELS,
                    recordingChannels
            );
        }

        Integer timeLimit =
                request.getTimeLimit();

        if (timeLimit == null) {

            timeLimit =
                    exotelProperties.getTimeLimit();
        }

        if (timeLimit != null) {

            formData.add(
                    FIELD_TIME_LIMIT,
                    String.valueOf(timeLimit)
            );
        }
    }

    /**
     * Adds the configured Exotel application flow.
     *
     * @param formData Exotel form data
     */
    private void addApplicationFlowConfiguration(
            MultiValueMap<String, String> formData) {

        String appUrl =
                exotelProperties.getAppUrl();

        if (appUrl == null
                || appUrl.isBlank()) {

            throw new IllegalStateException(
                    "Exotel application URL is not configured."
            );
        }

        formData.add(
                FIELD_URL,
                appUrl
        );
    }

    /**
     * Adds Exotel callback configuration.
     *
     * @param formData Exotel form data
     * @param request outbound call request
     */
    private void addCallbackConfiguration(
            MultiValueMap<String, String> formData,
            PlaceOutboundCallRequestDto request) {

        String callbackUrl =
                request.getCallbackUrl();

        if (callbackUrl == null
                || callbackUrl.isBlank()) {

            callbackUrl =
                    exotelProperties.getStatusCallbackUrl();
        }

        if (callbackUrl == null
                || callbackUrl.isBlank()) {

            return;
        }

        formData.add(
                FIELD_STATUS_CALLBACK,
                callbackUrl
        );

        formData.add(
                FIELD_STATUS_CALLBACK_EVENTS,
                "answered"
        );

        formData.add(
                FIELD_STATUS_CALLBACK_EVENTS,
                "terminal"
        );

        formData.add(
                FIELD_STATUS_CALLBACK_EVENTS,
                "ringing"
        );
    }

    /**
     * Checks whether a callback URL is configured.
     *
     * @param request outbound call request
     * @return true when callback URL is configured
     */
    private boolean isCallbackConfigured(
            PlaceOutboundCallRequestDto request) {

        if (request.getCallbackUrl() != null
                && !request.getCallbackUrl().isBlank()) {

            return true;
        }

        return exotelProperties.getStatusCallbackUrl() != null
                && !exotelProperties.getStatusCallbackUrl().isBlank();
    }

    /**
     * Converts an Exotel response into a provider-neutral DTO.
     *
     * @param responseBody Exotel response
     * @return provider call response
     */
    private ProviderCallResponseDto buildProviderCallResponse(
            String responseBody) {

        if (responseBody == null
                || responseBody.isBlank()) {

            throw new IllegalStateException(
                    "Exotel returned an empty call response."
            );
        }

        String trimmedResponse =
                responseBody.trim();

        if (trimmedResponse.startsWith("<")) {

            return buildProviderCallResponseFromXml(
                    trimmedResponse
            );
        }

        if (trimmedResponse.startsWith("{")
                || trimmedResponse.startsWith("[")) {

            return buildProviderCallResponseFromJson(
                    trimmedResponse
            );
        }

        throw new IllegalStateException(
                "Unknown Exotel call response format."
        );
    }

    /**
     * Parses an Exotel XML success response.
     *
     * @param responseBody XML response
     * @return provider call response
     */
    private ProviderCallResponseDto buildProviderCallResponseFromXml(
            String responseBody) {

        try {

            Document document =
                    parseXmlDocument(
                            responseBody
                    );

            String errorMessage =
                    getXmlElementText(
                            document,
                            "Message"
                    );

            NodeList restExceptionNodes =
                    document.getElementsByTagName(
                            "RestException"
                    );

            if (restExceptionNodes.getLength() > 0) {

                String errorStatus =
                        getXmlElementText(
                                document,
                                "Status"
                        );

                throw new IllegalStateException(
                        "Exotel rejected the call. "
                                + "status="
                                + firstNonBlank(
                                errorStatus,
                                "unknown"
                        )
                                + ", message="
                                + firstNonBlank(
                                errorMessage,
                                "Unknown Exotel error"
                        )
                );
            }

            String providerCallId =
                    firstNonBlank(
                            getXmlElementText(
                                    document,
                                    "Sid"
                            ),
                            getXmlElementText(
                                    document,
                                    "CallSid"
                            )
                    );

            String status =
                    getXmlElementText(
                            document,
                            "Status"
                    );

            if (providerCallId == null) {

                throw new IllegalStateException(
                        "Exotel response did not contain "
                                + "a call identifier."
                );
            }

            log.info(
                    "Exotel outbound call response parsed. "
                            + "providerCallId={}, providerStatus={}",
                    providerCallId,
                    status
            );

            return ProviderCallResponseDto.builder()
                    .provider(PROVIDER_CODE)
                    .providerCallId(providerCallId)
                    .status(status)
                    .build();

        } catch (IllegalStateException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Unable to parse Exotel XML call response.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to parse Exotel XML call response.",
                    exception
            );
        }
    }

    /**
     * Parses an Exotel JSON response.
     *
     * @param responseBody JSON response
     * @return provider call response
     */
    private ProviderCallResponseDto buildProviderCallResponseFromJson(
            String responseBody) {

        try {

            JsonNode root =
                    objectMapper.readTree(
                            responseBody
                    );

            JsonNode callNode =
                    root.path("Call");

            String providerCallId =
                    firstNonBlank(
                            callNode.path("Sid")
                                    .asText(null),
                            callNode.path("CallSid")
                                    .asText(null),
                            root.path("Sid")
                                    .asText(null),
                            root.path("CallSid")
                                    .asText(null)
                    );

            String status =
                    firstNonBlank(
                            callNode.path("Status")
                                    .asText(null),
                            root.path("Status")
                                    .asText(null)
                    );

            if (providerCallId == null) {

                throw new IllegalStateException(
                        "Exotel JSON response did not contain "
                                + "a call identifier."
                );
            }

            log.info(
                    "Exotel JSON outbound call response parsed. "
                            + "providerCallId={}, providerStatus={}",
                    providerCallId,
                    status
            );

            return ProviderCallResponseDto.builder()
                    .provider(PROVIDER_CODE)
                    .providerCallId(providerCallId)
                    .status(status)
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Unable to parse Exotel JSON call response.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to parse Exotel JSON call response.",
                    exception
            );
        }
    }

    /**
     * Parses an XML response securely.
     *
     * @param responseBody XML response
     * @return parsed XML document
     * @throws Exception when XML parsing fails
     */
    private Document parseXmlDocument(
            String responseBody) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        return builder.parse(
                new InputSource(
                        new StringReader(
                                responseBody
                        )
                )
        );
    }

    /**
     * Extracts an XML element's text.
     *
     * @param document XML document
     * @param elementName element name
     * @return element value
     */
    private String getXmlElementText(
            Document document,
            String elementName) {

        NodeList nodes =
                document.getElementsByTagName(
                        elementName
                );

        if (nodes.getLength() == 0) {

            return null;
        }

        Node node =
                nodes.item(0);

        if (node == null) {

            return null;
        }

        String value =
                node.getTextContent();

        if (value == null) {

            return null;
        }

        value =
                value.trim();

        return value.isBlank()
                ? null
                : value;
    }

    /**
     * Parses an Exotel error response.
     *
     * @param responseBody Exotel error response
     * @return readable error
     */
    private String parseExotelErrorResponse(
            String responseBody) {

        if (responseBody == null
                || responseBody.isBlank()) {

            return "Empty response from Exotel.";
        }

        String trimmed =
                responseBody.trim();

        if (trimmed.startsWith("<")) {

            try {

                Document document =
                        parseXmlDocument(
                                trimmed
                        );

                String status =
                        getXmlElementText(
                                document,
                                "Status"
                        );

                String message =
                        getXmlElementText(
                                document,
                                "Message"
                        );

                return firstNonBlank(
                        message,
                        status != null
                                ? "Exotel returned status "
                                + status
                                : "Unknown Exotel XML error."
                );

            } catch (Exception exception) {

                log.warn(
                        "Unable to parse Exotel XML error response.",
                        exception
                );
            }
        }

        if (trimmed.startsWith("{")
                || trimmed.startsWith("[")) {

            try {

                JsonNode root =
                        objectMapper.readTree(
                                trimmed
                        );

                String message =
                        root.path("Message")
                                .asText(null);

                if (message == null) {

                    message =
                            root.path("message")
                                    .asText(null);
                }

                if (message != null
                        && !message.isBlank()) {

                    return message;
                }

            } catch (Exception exception) {

                log.debug(
                        "Unable to parse Exotel JSON error response.",
                        exception
                );
            }
        }

        return trimmed.length() > 500
                ? trimmed.substring(0, 500)
                : trimmed;
    }

    /**
     * Provisions a telephony number.
     *
     * @param request number provisioning request
     * @return number response
     */
    @Override
    public NumberResponseDto provisionNumber(
            ProvisionNumberRequestDto request) {

        log.info(
                "Exotel number provisioning requested. "
                        + "region={}, type={}",
                request != null
                        ? request.getRegion()
                        : null,
                request != null
                        ? request.getType()
                        : null
        );

        throw new UnsupportedOperationException(
                "Exotel number provisioning is managed "
                        + "through the Exotel dashboard."
        );
    }

    /**
     * Transfers an active call.
     *
     * @param request transfer request
     */
    /**
     * Executes the Exotel-specific part of an application-controlled
     * active call transfer.
     *
     * <p>
     * Exotel does not expose an active-call transfer REST operation
     * that should be invoked from this provider adapter. The active
     * Voicebot media session is closed by the application transfer
     * service, allowing Exotel to continue with the next configured
     * applet. That applet calls the application's transfer endpoint
     * to retrieve the destination selected by the Flow.
     * </p>
     *
     * <p>
     * This method therefore validates and records the provider-side
     * transfer execution point without creating a new call through
     * the Exotel Connect API.
     * </p>
     *
     * @param request transfer request
     */
    @Override
    public void transferCall(
            TransferCallRequestDto request) {

        if (request == null) {

            log.error(
                    "Cannot execute Exotel transfer because "
                            + "transfer request is null."
            );

            throw new IllegalArgumentException(
                    "Transfer request is required."
            );
        }

        if (request.getProviderCallId() == null
                || request.getProviderCallId().isBlank()) {

            log.error(
                    "Cannot execute Exotel transfer because "
                            + "provider call ID is missing."
            );

            throw new IllegalArgumentException(
                    "Provider call ID is required for transfer."
            );
        }

        if (request.getDestination() == null
                || request.getDestination().isBlank()) {

            log.error(
                    "Cannot execute Exotel transfer because "
                            + "destination is missing. providerCallId={}",
                    request.getProviderCallId()
            );

            throw new IllegalArgumentException(
                    "Transfer destination is required."
            );
        }

        log.info(
                "Exotel application-controlled transfer prepared. "
                        + "providerCallId={}, destination={}",
                request.getProviderCallId(),
                request.getDestination()
        );

        /*
         * The actual Exotel handoff is performed through the configured
         * Exotel application flow:
         *
         * 1. Active Voicebot WebSocket is closed.
         * 2. Exotel continues to the next applet.
         * 3. Exotel invokes our transfer endpoint.
         * 4. Our application returns the persisted destination.
         * 5. Exotel Connect/Dial applet connects the destination.
         *
         * Do not call Calls/connect here because that would create
         * another outbound call rather than transfer the active call.
         */
    }

    /**
     * Hangs up an active call.
     *
     * @param request hangup request
     */
    @Override
    public void hangupCall(
            HangupCallRequestDto request) {

        log.info(
                "Exotel call hangup requested. callId={}",
                request != null
                        ? request.getProviderCallId()
                        : null
        );

        throw new UnsupportedOperationException(
                "Exotel call hangup is not implemented "
                        + "in the current telephony integration."
        );
    }

    /**
     * Normalizes an Exotel call status callback.
     *
     * @param payload provider callback payload
     * @return normalized call event
     */
    @Override
    public NormalizedCallEventDto normalizeCallEvent(
            String payload) {

        log.info(
                "Normalizing Exotel call webhook."
        );

        Map<String, String> callbackParameters =
                parseCallbackPayload(
                        payload
                );

        String callSid =
                callbackParameters.get(
                        FIELD_CALL_SID
                );

        String status =
                callbackParameters.get(
                        FIELD_STATUS
                );

        String fromNumber =
                callbackParameters.get(
                        FIELD_FROM
                );

        String toNumber =
                callbackParameters.get(
                        FIELD_TO
                );

        Instant timestamp =
                parseTimestamp(
                        callbackParameters.get(
                                FIELD_DATE_UPDATED
                        )
                );

        String event =
                resolveEvent(
                        status
                );

        log.info(
                "Exotel webhook normalized. "
                        + "providerCallId={}, providerStatus={}, "
                        + "normalizedEvent={}",
                callSid,
                status,
                event
        );

        return NormalizedCallEventDto.builder()
                .event(event)
                .provider(PROVIDER_CODE)
                .providerEventId(callSid)
                .providerCallId(callSid)
                .fromNumber(fromNumber)
                .toNumber(toNumber)
                .timestamp(timestamp)
                .payload(payload)
                .build();
    }

    /**
     * Parses an Exotel URL-encoded callback body.
     *
     * @param payload callback payload
     * @return callback parameters
     */
    private Map<String, String> parseCallbackPayload(
            String payload) {

        Map<String, String> parameters =
                new HashMap<>();

        if (payload == null
                || payload.isBlank()) {

            log.warn(
                    "Received empty Exotel webhook payload."
            );

            return parameters;
        }

        String[] pairs =
                payload.split("&");

        for (String pair : pairs) {

            if (pair == null
                    || pair.isBlank()) {

                continue;
            }

            String[] keyValue =
                    pair.split(
                            "=",
                            2
                    );

            String key =
                    URLDecoder.decode(
                            keyValue[0],
                            StandardCharsets.UTF_8
                    );

            String value =
                    keyValue.length > 1
                            ? URLDecoder.decode(
                            keyValue[1],
                            StandardCharsets.UTF_8
                    )
                            : "";

            parameters.put(
                    key,
                    value
            );
        }

        log.debug(
                "Exotel webhook parameters parsed. "
                        + "parameterCount={}",
                parameters.size()
        );

        return parameters;
    }

    /**
     * Resolves the normalized platform event from an Exotel status.
     *
     * @param status Exotel provider status
     * @return normalized platform event
     */
    private String resolveEvent(
            String status) {

        if (status == null
                || status.isBlank()) {

            log.warn(
                    "Exotel webhook received without call status."
            );

            return TelephonyConstants.EVENT_CALL_ENDED;
        }

        String normalizedStatus =
                status.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        String event =
                switch (normalizedStatus) {

                    case "initiated",
                         "queued" ->
                            TelephonyConstants.EVENT_CALL_INITIATED;

                    case "ringing" ->
                            TelephonyConstants.EVENT_CALL_RINGING;

                    case "in-progress",
                         "answered" ->
                            TelephonyConstants.EVENT_CALL_ANSWERED;

                    case "completed" ->
                            TelephonyConstants.EVENT_CALL_COMPLETED;

                    case "failed" ->
                            TelephonyConstants.EVENT_CALL_FAILED;

                    case "busy" ->
                            TelephonyConstants.EVENT_CALL_BUSY;

                    case "no-answer",
                         "no_answer" ->
                            TelephonyConstants.EVENT_CALL_NO_ANSWER;

                    case "canceled",
                         "cancelled",
                         "canceled-by-caller",
                         "cancelled-by-caller" ->
                            TelephonyConstants.EVENT_CALL_CANCELLED;

                    case "rejected" ->
                            TelephonyConstants.EVENT_CALL_REJECTED;

                    default -> {

                        log.warn(
                                "Unknown Exotel call status received. "
                                        + "status={}",
                                status
                        );

                        yield TelephonyConstants.EVENT_CALL_ENDED;
                    }
                };

        log.debug(
                "Exotel call status mapped. "
                        + "providerStatus={}, normalizedEvent={}",
                status,
                event
        );

        return event;
    }

    /**
     * Parses an Exotel callback timestamp.
     *
     * @param value Exotel timestamp
     * @return UTC timestamp
     */
    private Instant parseTimestamp(
            String value) {

        if (value == null
                || value.isBlank()) {

            return Instant.now();
        }

        try {

            return LocalDateTime.parse(
                            value,
                            EXOTEL_DATE_TIME_FORMATTER
                    )
                    .toInstant(
                            ZoneOffset.UTC
                    );

        } catch (Exception exception) {

            log.warn(
                    "Unable to parse Exotel callback timestamp. "
                            + "value={}",
                    value,
                    exception
            );

            return Instant.now();
        }
    }

    /**
     * Returns the first non-blank value.
     *
     * @param values candidate values
     * @return first non-blank value
     */
    private String firstNonBlank(
            String... values) {

        for (String value : values) {

            if (value != null
                    && !value.isBlank()) {

                return value;
            }
        }

        return null;
    }

    /**
     * Returns phone numbers currently owned by the Exotel account.
     *
     * @return list of Exotel-owned phone numbers
     */
    @Override
    public List<NumberResponseDto> getOwnedNumbers() {

        validateNumberApiConfiguration();

        String path =
                exotelProperties
                        .getIncomingPhoneNumbersPath()
                        .replace(
                                "{accountSid}",
                                exotelProperties.getAccountSid()
                        );

        String requestUrl =
                exotelProperties.getBaseUrl() + path;

        log.info(
                "Fetching owned Exotel phone numbers. requestUrl={}",
                requestUrl
        );

        try {

            String response =
                    exotelRestClient
                            .get()
                            .uri(requestUrl)
                            .headers(
                                    headers ->
                                            headers.setBasicAuth(
                                                    exotelProperties.getApiKey(),
                                                    exotelProperties.getApiToken()
                                            )
                            )
                            .retrieve()
                            .body(String.class);

            return parsePhoneNumbers(
                    response
            );

        } catch (RestClientResponseException exception) {

            log.error(
                    "Exotel owned-number request failed. "
                            + "requestUrl={}, httpStatus={}",
                    requestUrl,
                    exception.getStatusCode().value(),
                    exception
            );

            throw new IllegalStateException(
                    "Unable to fetch phone numbers from Exotel.",
                    exception
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch owned Exotel phone numbers. "
                            + "requestUrl={}",
                    requestUrl,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to fetch phone numbers from Exotel.",
                    exception
            );
        }
    }

    /**
     * Validates Exotel number API configuration.
     */
    private void validateNumberApiConfiguration() {

        if (exotelProperties.getBaseUrl() == null
                || exotelProperties.getBaseUrl().isBlank()) {

            throw new IllegalStateException(
                    "Exotel base URL is not configured."
            );
        }

        if (exotelProperties.getIncomingPhoneNumbersPath() == null
                || exotelProperties
                .getIncomingPhoneNumbersPath()
                .isBlank()) {

            throw new IllegalStateException(
                    "Exotel incoming phone numbers path "
                            + "is not configured."
            );
        }

        if (exotelProperties.getAccountSid() == null
                || exotelProperties.getAccountSid().isBlank()) {

            throw new IllegalStateException(
                    "Exotel account SID is not configured."
            );
        }
    }

    /**
     * Returns phone numbers available for provisioning from Exotel.
     *
     * @param request provisioning criteria
     * @return available phone numbers
     */
    /**
     * Retrieves phone numbers currently available from Exotel.
     *
     * <p>
     * The incoming request uses platform-level search criteria.
     * Exotel-specific request parameters are created only inside
     * this provider implementation.
     * </p>
     *
     * @param request provider-independent number search criteria
     * @return list of normalized available phone numbers
     */
    @Override
    public List<NumberResponseDto> getAvailableNumbers(
            NumberSearchRequestDto request) {

        validateAvailableNumberRequest(request);
        validateNumberApiConfiguration();

        String path =
                exotelProperties
                        .getAvailablePhoneNumbersPath()
                        .replace(
                                "{accountSid}",
                                exotelProperties.getAccountSid()
                        )
                        .replace(
                                "{countryCode}",
                                request.getCountryCode()
                        )
                        .replace(
                                "{numberType}",
                                mapExotelNumberType(
                                        request.getNumberType()
                                )
                        );

        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder
                        .fromUriString(
                                exotelProperties.getBaseUrl()
                                        + path
                        );

        if (request.getRegion() != null
                && !request.getRegion().isBlank()) {

            uriBuilder.queryParam(
                    "InRegion",
                    request.getRegion()
            );
        }

        if (request.getSearchPattern() != null
                && !request.getSearchPattern().isBlank()) {

            uriBuilder.queryParam(
                    "Contains",
                    request.getSearchPattern()
            );
        }

        if (request.getSmsEnabled() != null) {

            uriBuilder.queryParam(
                    "IncomingSMS",
                    request.getSmsEnabled()
            );
        }

        String requestUri =
                uriBuilder
                        .build()
                        .encode()
                        .toUriString();

        log.info(
                "Fetching available Exotel phone numbers. "
                        + "countryCode={}, region={}, numberType={}",
                request.getCountryCode(),
                request.getRegion(),
                request.getNumberType()
        );

        try {

            String response =
                    exotelRestClient
                            .get()
                            .uri(requestUri)
                            .headers(
                                    headers ->
                                            headers.setBasicAuth(
                                                    exotelProperties.getApiKey(),
                                                    exotelProperties.getApiToken()
                                            )
                            )
                            .retrieve()
                            .body(String.class);

            return parseAvailablePhoneNumbers(
                    response
            );

        } catch (RestClientResponseException exception) {

            log.error(
                    "Exotel available-number request failed. "
                            + "httpStatus={}",
                    exception.getStatusCode().value(),
                    exception
            );

            throw new IllegalStateException(
                    "Unable to fetch available phone numbers from Exotel.",
                    exception
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch available Exotel phone numbers.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to fetch available phone numbers from Exotel.",
                    exception
            );
        }
    }

    /**
     * Parses the Exotel incoming phone numbers response.
     *
     * @param response raw JSON response returned by Exotel
     * @return list of normalized phone number responses
     */
    private List<NumberResponseDto> parsePhoneNumbers(
            String response) {

        if (response == null
                || response.isBlank()) {

            log.warn(
                    "Exotel phone-number response is empty."
            );

            return List.of();
        }

        try {

            JsonNode rootNode =
                    objectMapper.readTree(
                            response
                    );

            JsonNode phoneNumbersNode =
                    rootNode.path(
                            "incoming_phone_numbers"
                    );

            if (!phoneNumbersNode.isArray()) {

                log.warn(
                        "Exotel response does not contain "
                                + "incoming_phone_numbers array."
                );

                return List.of();
            }

            List<NumberResponseDto> phoneNumbers =
                    new ArrayList<>();

            for (JsonNode phoneNumberNode :
                    phoneNumbersNode) {

                NumberResponseDto numberResponse =
                        new NumberResponseDto();

                numberResponse.setProvider(
                        getProviderCode()
                );

                numberResponse.setProviderNumberId(
                        phoneNumberNode
                                .path("sid")
                                .asText(null)
                );

                numberResponse.setE164Number(
                        phoneNumberNode
                                .path("phone_number")
                                .asText(null)
                );

                numberResponse.setType(
                        phoneNumberNode
                                .path("number_type")
                                .asText(null)
                );

                /*
                 * Exotel does not provide a dedicated status
                 * field in the incoming phone-number response.
                 */
                numberResponse.setStatus(
                        null
                );

                phoneNumbers.add(
                        numberResponse
                );
            }

            log.info(
                    "Parsed {} owned Exotel phone numbers.",
                    phoneNumbers.size()
            );

            return phoneNumbers;

        } catch (Exception exception) {

            log.error(
                    "Failed to parse Exotel phone numbers response.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to parse phone numbers received from Exotel.",
                    exception
            );
        }
    }

    /**
     * Validates the available-number search request.
     *
     * @param request number search request
     */
    private void validateAvailableNumberRequest(
            NumberSearchRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Number search request must not be null."
            );
        }

        if (request.getCountryCode() == null
                || request.getCountryCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Country code is required."
            );
        }

        if (request.getNumberType() == null
                || request.getNumberType().isBlank()) {

            throw new IllegalArgumentException(
                    "Number type is required."
            );
        }
    }

    /**
     * Maps the platform-level number type to the
     * corresponding Exotel number type.
     *
     * @param numberType platform-level number type
     * @return Exotel number type
     */
    private String mapExotelNumberType(
            String numberType) {

        String normalizedType =
                numberType
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace("-", "")
                        .replace("_", "")
                        .replace(" ", "");

        return switch (normalizedType) {

            case "LANDLINE" ->
                    "Landline";

            case "MOBILE" ->
                    "Mobile";

            case "TOLLFREE" ->
                    "TollFree";

            default -> throw new IllegalArgumentException(
                    "Unsupported number type: "
                            + numberType
            );
        };
    }

    /**
     * Parses the Exotel available phone numbers response.
     *
     * @param response raw JSON response returned by Exotel
     * @return normalized available phone numbers
     */
    private List<NumberResponseDto> parseAvailablePhoneNumbers(
            String response) {

        if (response == null
                || response.isBlank()) {

            log.warn(
                    "Exotel available phone-number response is empty."
            );

            return List.of();
        }

        try {

            JsonNode rootNode =
                    objectMapper.readTree(
                            response
                    );

            JsonNode phoneNumbersNode =
                    rootNode.path(
                            "available_phone_numbers"
                    );

            if (!phoneNumbersNode.isArray()) {

                /*
                 * Some provider responses can return the
                 * collection directly as an array.
                 */
                if (rootNode.isArray()) {
                    phoneNumbersNode = rootNode;
                } else {

                    log.warn(
                            "Exotel response does not contain "
                                    + "available_phone_numbers array."
                    );

                    return List.of();
                }
            }

            List<NumberResponseDto> phoneNumbers =
                    new ArrayList<>();

            for (JsonNode phoneNumberNode :
                    phoneNumbersNode) {

                NumberResponseDto numberResponse =
                        new NumberResponseDto();

                numberResponse.setProvider(
                        getProviderCode()
                );

                numberResponse.setE164Number(
                        phoneNumberNode
                                .path("phone_number")
                                .asText(null)
                );

                numberResponse.setType(
                        phoneNumberNode
                                .path("number_type")
                                .asText(null)
                );

                numberResponse.setStatus(
                        "AVAILABLE"
                );

                /*
                 * The available-number response does not
                 * provide an Exotel IncomingPhoneNumber SID.
                 * The SID is available after purchase.
                 */
                numberResponse.setProviderNumberId(
                        phoneNumberNode
                                .path("sid")
                                .asText(null)
                );

                phoneNumbers.add(
                        numberResponse
                );
            }

            log.info(
                    "Parsed {} available Exotel phone numbers.",
                    phoneNumbers.size()
            );

            return phoneNumbers;

        } catch (Exception exception) {

            log.error(
                    "Failed to parse Exotel available "
                            + "phone numbers response.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to parse available phone numbers "
                            + "received from Exotel.",
                    exception
            );
        }
    }
}