package com.infinitio.aivoiceplatform.telephony.provider.impl;

import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import com.infinitio.aivoiceplatform.telephony.dto.request.HangupCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.ProvisionNumberRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;
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
 * Provider-specific statuses must never be propagated directly
 * to the rest of the application. They are converted into the
 * normalized telephony events defined in {@link TelephonyConstants}.
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

    private static final String FIELD_FROM =
            "From";

    private static final String FIELD_CALLER_ID =
            "CallerId";

    private static final String FIELD_URL =
            "Url";

    private static final String FIELD_STATUS_CALLBACK =
            "StatusCallback";

    private static final String FIELD_CALL_SID =
            "CallSid";

    private static final String FIELD_STATUS =
            "Status";

    private static final String FIELD_RECORDING_URL =
            "RecordingUrl";

    private static final String FIELD_DATE_UPDATED =
            "DateUpdated";

    private static final String FIELD_TO =
            "To";

    private static final String FIELD_FROM_NUMBER =
            "From";

    private static final DateTimeFormatter EXOTEL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

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
     * Exotel first calls the number supplied in the From field.
     * Once that party answers, Exotel connects the call to the
     * configured Exotel application flow.
     *
     * @param request outbound call request
     * @return provider call response
     */
    @Override
    public ProviderCallResponseDto placeOutboundCall(
            PlaceOutboundCallRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Outbound call request is required."
            );
        }

        log.info(
                "Placing outbound Exotel call. from={}, to={}, realtime={}",
                request.getFromNumber(),
                request.getToNumber(),
                request.getStreamUrl() != null
                        && !request.getStreamUrl().isBlank()
        );

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        /*
         * ---------------------------------------------------------
         * EXOTEL CONNECT API
         * ---------------------------------------------------------
         *
         * From     = number that Exotel calls first
         * CallerId = Exotel virtual number
         * To       = destination number
         */

        formData.add(
                FIELD_FROM,
                request.getToNumber()
        );

        formData.add(
                FIELD_CALLER_ID,
                request.getFromNumber()
        );

        /*
         * ---------------------------------------------------------
         * REALTIME BIDIRECTIONAL VOICE AI
         * ---------------------------------------------------------
         */

        boolean realtime =
                request.getStreamUrl() != null
                        && !request.getStreamUrl().isBlank();

        if (realtime) {

            formData.add(
                    "StreamUrl",
                    request.getStreamUrl()
            );

            formData.add(
                    "StreamType",
                    request.getStreamType() != null
                            && !request.getStreamType().isBlank()
                            ? request.getStreamType()
                            : exotelProperties.getStreamType()
            );

            if (Boolean.TRUE.equals(request.getRecord())) {

                formData.add(
                        "Record",
                        "true"
                );
            }

            if (request.getRecordingChannels() != null
                    && !request.getRecordingChannels().isBlank()) {

                formData.add(
                        "RecordingChannels",
                        request.getRecordingChannels()
                );
            }

            if (request.getTimeLimit() != null) {

                formData.add(
                        "TimeLimit",
                        String.valueOf(
                                request.getTimeLimit()
                        )
                );
            }

        } else {

            /*
             * -----------------------------------------------------
             * EXISTING EXOTEL APPLICATION FLOW
             * -----------------------------------------------------
             */

            formData.add(
                    FIELD_URL,
                    exotelProperties.getAppUrl()
            );
        }

        /*
         * ---------------------------------------------------------
         * STATUS CALLBACK
         * ---------------------------------------------------------
         */

        String callbackUrl =
                request.getCallbackUrl();

        if (callbackUrl == null
                || callbackUrl.isBlank()) {

            callbackUrl =
                    exotelProperties.getStatusCallbackUrl();
        }

        if (callbackUrl != null
                && !callbackUrl.isBlank()) {

            formData.add(
                    FIELD_STATUS_CALLBACK,
                    callbackUrl
            );

            /*
             * Exotel supports these callback events.
             */

            formData.add(
                    "StatusCallbackEvents[]",
                    "answered"
            );

            formData.add(
                    "StatusCallbackEvents[]",
                    "terminal"
            );

            formData.add(
                    "StatusCallbackEvents[]",
                    "ringing"
            );
        }

        log.info(
                "Sending Exotel outbound request. "
                        + "realtime={}, callbackConfigured={}, "
                        + "streamConfigured={}",
                realtime,
                callbackUrl != null
                        && !callbackUrl.isBlank(),
                request.getStreamUrl() != null
                        && !request.getStreamUrl().isBlank()
        );

        /*
         * ---------------------------------------------------------
         * CALL EXOTEL
         * ---------------------------------------------------------
         *
         * Exotel's Connect API uses:
         *
         * POST /v1/Accounts/{accountSid}/Calls/connect
         *
         * The response is XML.
         */

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
                                                    exotelProperties
                                                            .getApiKey(),
                                                    exotelProperties
                                                            .getApiToken()
                                            )
                            )
                            .contentType(
                                    MediaType.APPLICATION_FORM_URLENCODED
                            )
                            .body(formData)
                            .retrieve()
                            .body(String.class);

            log.info("Exotel raw outbound response: {}", responseBody);

        } catch (RestClientResponseException exception) {

            /*
             * Exotel returns XML even for many error responses.
             *
             * Example:
             *
             * <TwilioResponse>
             *     <RestException>
             *         <Status>400</Status>
             *         <Message>...</Message>
             *     </RestException>
             * </TwilioResponse>
             */

            String errorBody =
                    exception.getResponseBodyAsString();

            String exotelError =
                    parseExotelErrorResponse(
                            errorBody
                    );

            log.error(
                    "Exotel outbound call request failed. "
                            + "httpStatus={}, exotelError={}",
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

        /*
         * ---------------------------------------------------------
         * PARSE EXOTEL RESPONSE
         * ---------------------------------------------------------
         */

        return buildProviderCallResponse(
                responseBody
        );
    }

    /**
     * Converts Exotel call response into the provider-neutral
     * response DTO.
     *
     * <p>
     * Exotel's Calls/connect API returns XML:
     *
     * <pre>
     * &lt;TwilioResponse&gt;
     *     &lt;Call&gt;
     *         &lt;Sid&gt;xxxxxxxx&lt;/Sid&gt;
     *         &lt;Status&gt;in-progress&lt;/Status&gt;
     *     &lt;/Call&gt;
     * &lt;/TwilioResponse&gt;
     * </pre>
     *
     * <p>
     * JSON parsing is retained as a fallback because some
     * Exotel APIs may return JSON depending on endpoint/version.
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

        /*
         * ---------------------------------------------------------
         * XML RESPONSE
         * ---------------------------------------------------------
         */

        if (trimmedResponse.startsWith("<")) {

            return buildProviderCallResponseFromXml(
                    trimmedResponse
            );
        }

        /*
         * ---------------------------------------------------------
         * JSON FALLBACK
         * ---------------------------------------------------------
         */

        if (trimmedResponse.startsWith("{")
                || trimmedResponse.startsWith("[")) {

            return buildProviderCallResponseFromJson(
                    trimmedResponse
            );
        }

        log.error(
                "Unknown Exotel response format. "
                        + "responsePrefix={}",
                trimmedResponse.substring(
                        0,
                        Math.min(
                                trimmedResponse.length(),
                                100
                        )
                )
        );

        throw new IllegalStateException(
                "Unknown Exotel call response format."
        );
    }

    /**
     * Parses Exotel XML success response.
     *
     * @param responseBody XML response
     * @return provider call response
     */
    private ProviderCallResponseDto buildProviderCallResponseFromXml(
            String responseBody) {

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            /*
             * -----------------------------------------------------
             * XML SECURITY
             * -----------------------------------------------------
             *
             * Disable external entities and external DTDs.
             */

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

            Document document =
                    builder.parse(
                            new InputSource(
                                    new StringReader(
                                            responseBody
                                    )
                            )
                    );

            /*
             * -----------------------------------------------------
             * CHECK FOR EXOTEL ERROR RESPONSE
             * -----------------------------------------------------
             */

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

            /*
             * -----------------------------------------------------
             * EXTRACT CALL
             * -----------------------------------------------------
             */

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

                log.error(
                        "Exotel XML response did not contain "
                                + "a Call Sid. response={}",
                        responseBody
                );

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
                    .provider(
                            PROVIDER_CODE
                    )
                    .providerCallId(
                            providerCallId
                    )
                    .status(
                            status
                    )
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
     * Parses JSON response as a fallback.
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
                    .provider(
                            PROVIDER_CODE
                    )
                    .providerCallId(
                            providerCallId
                    )
                    .status(
                            status
                    )
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
     * Extracts an element's text from an XML document.
     *
     * @param document XML document
     * @param elementName element name
     * @return element text
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
     * Parses Exotel error response.
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

        /*
         * XML error response.
         */

        if (trimmed.startsWith("<")) {

            try {

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

                Document document =
                        builder.parse(
                                new InputSource(
                                        new StringReader(
                                                trimmed
                                        )
                                )
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

        /*
         * JSON error fallback.
         */

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

        /*
         * Last fallback.
         */

        return trimmed.length() > 500
                ? trimmed.substring(
                0,
                500
        )
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
                request.getRegion(),
                request.getType()
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
    @Override
    public void transferCall(
            TransferCallRequestDto request) {

        log.info(
                "Exotel call transfer requested. "
                        + "callId={}, destination={}",
                request.getProviderCallId(),
                request.getDestination()
        );

        throw new UnsupportedOperationException(
                "Exotel call transfer is not implemented "
                        + "in the current telephony integration."
        );
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
                request.getProviderCallId()
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
                        FIELD_FROM_NUMBER
                );

        String toNumber =
                callbackParameters.get(
                        FIELD_TO
                );

        String recordingUrl =
                callbackParameters.get(
                        FIELD_RECORDING_URL
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
                        + "normalizedEvent={}, recordingPresent={}",
                callSid,
                status,
                event,
                recordingUrl != null
                        && !recordingUrl.isBlank()
        );

        return NormalizedCallEventDto.builder()
                .event(
                        event
                )
                .provider(
                        PROVIDER_CODE
                )
                .providerEventId(
                        callSid
                )
                .providerCallId(
                        callSid
                )
                .fromNumber(
                        fromNumber
                )
                .toNumber(
                        toNumber
                )
                .timestamp(
                        timestamp
                )
                .payload(
                        payload
                )
                .build();
    }

    /**
     * Parses Exotel's URL-encoded callback body.
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
     * Resolves the normalized platform event from Exotel status.
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
                                java.util.Locale.ROOT
                        );

        String event =
                switch (normalizedStatus) {

                    case "initiated",
                         "queued" ->

                            TelephonyConstants
                                    .EVENT_CALL_INITIATED;

                    case "ringing" ->

                            TelephonyConstants
                                    .EVENT_CALL_RINGING;

                    case "in-progress",
                         "answered" ->

                            TelephonyConstants
                                    .EVENT_CALL_ANSWERED;

                    case "completed" ->

                            TelephonyConstants
                                    .EVENT_CALL_COMPLETED;

                    case "failed" ->

                            TelephonyConstants
                                    .EVENT_CALL_FAILED;

                    case "busy" ->

                            TelephonyConstants
                                    .EVENT_CALL_BUSY;

                    case "no-answer",
                         "no_answer" ->

                            TelephonyConstants
                                    .EVENT_CALL_NO_ANSWER;

                    case "canceled",
                         "cancelled",
                         "canceled-by-caller",
                         "cancelled-by-caller" ->

                            TelephonyConstants
                                    .EVENT_CALL_CANCELLED;

                    case "rejected" ->

                            TelephonyConstants
                                    .EVENT_CALL_REJECTED;

                    default -> {

                        log.warn(
                                "Unknown Exotel call status received. "
                                        + "status={}",
                                status
                        );

                        yield TelephonyConstants
                                .EVENT_CALL_ENDED;
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
     * Parses Exotel callback timestamp.
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
}