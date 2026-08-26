package com.infinitio.aivoiceplatform.telephony.provider.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.infinitio.aivoiceplatform.telephony.config.ExotelProperties;
import com.infinitio.aivoiceplatform.telephony.constant.TelephonyConstants;
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
public class ExotelTelephonyProvider
        implements TelephonyProvider {

    private static final String PROVIDER_CODE =
            TelephonyConstants.PROVIDER_EXOTEL;

    private static final String CONNECT_CALL_PATH =
            "/v1/Accounts/{accountSid}/Calls/connect.json";

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
     * </p>
     *
     * @param request outbound call request
     * @return provider call response
     */
    @Override
    public ProviderCallResponseDto placeOutboundCall(
            PlaceOutboundCallRequestDto request) {

        log.info(
                "Placing outbound Exotel call. from={}, to={}",
                request.getFromNumber(),
                request.getToNumber()
        );

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        /*
         * Exotel Connect API:
         *
         * From     = number that Exotel calls first
         * CallerId = ExoPhone displayed as caller ID
         * Url      = Exotel application flow
         *
         * For AI Dialer:
         *
         * request.toNumber = customer number.
         */
        formData.add(
                FIELD_FROM,
                request.getToNumber()
        );

        formData.add(
                FIELD_CALLER_ID,
                exotelProperties.getCallerId()
        );

        formData.add(
                FIELD_URL,
                exotelProperties.getAppUrl()
        );

        String callbackUrl =
                request.getCallbackUrl();

        if (callbackUrl == null
                || callbackUrl.isBlank()) {

            callbackUrl =
                    exotelProperties
                            .getStatusCallbackUrl();
        }

        if (callbackUrl != null
                && !callbackUrl.isBlank()) {

            formData.add(
                    FIELD_STATUS_CALLBACK,
                    callbackUrl
            );
        }

        log.debug(
                "Sending Exotel outbound call request. "
                        + "callbackConfigured={}",
                callbackUrl != null
                        && !callbackUrl.isBlank()
        );

        String responseBody =
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

        log.debug(
                "Exotel outbound call response received."
        );

        return buildProviderCallResponse(
                responseBody
        );
    }

    /**
     * Converts Exotel call response into the provider-neutral
     * response DTO.
     *
     * @param responseBody Exotel response
     * @return provider call response
     */
    private ProviderCallResponseDto buildProviderCallResponse(
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

                            root.path("Sid")
                                    .asText(null)
                    );

            String status =
                    firstNonBlank(
                            callNode.path("Status")
                                    .asText(null),

                            root.path("Status")
                                    .asText(null)
                    );

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

        } catch (Exception exception) {

            log.error(
                    "Unable to parse Exotel call response.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to parse Exotel call response.",
                    exception
            );
        }
    }

    /**
     * Provisions a telephony number.
     *
     * <p>
     * ExoPhones for the current integration are provisioned
     * through the Exotel dashboard.
     * </p>
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
     * <p>
     * Exotel sends callback parameters such as:
     *
     * <ul>
     *     <li>CallSid</li>
     *     <li>Status</li>
     *     <li>RecordingUrl</li>
     *     <li>DateUpdated</li>
     *     <li>From</li>
     *     <li>To</li>
     * </ul>
     *
     * <p>
     * The provider status is converted into a platform-neutral
     * event before being passed to the telephony service.
     * </p>
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
     * Resolves the normalized platform event from the
     * Exotel call status.
     *
     * <p>
     * This method is intentionally provider-specific.
     * No Exotel status should be directly exposed to
     * downstream application services.
     * </p>
     *
     * <p>
     * Supported statuses include:
     *
     * <ul>
     *     <li>initiated</li>
     *     <li>queued</li>
     *     <li>ringing</li>
     *     <li>in-progress</li>
     *     <li>answered</li>
     *     <li>completed</li>
     *     <li>failed</li>
     *     <li>busy</li>
     *     <li>no-answer</li>
     *     <li>canceled</li>
     *     <li>cancelled</li>
     *     <li>rejected</li>
     * </ul>
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

                    /*
                     * -------------------------------------------------
                     * CALL INITIATION
                     * -------------------------------------------------
                     */
                    case "initiated",
                         "queued" ->

                            TelephonyConstants
                                    .EVENT_CALL_INITIATED;

                    /*
                     * -------------------------------------------------
                     * CALL RINGING
                     * -------------------------------------------------
                     */
                    case "ringing" ->

                            TelephonyConstants
                                    .EVENT_CALL_RINGING;

                    /*
                     * -------------------------------------------------
                     * CALL ANSWERED
                     * -------------------------------------------------
                     *
                     * Exotel commonly exposes the active call
                     * state as in-progress. For our platform,
                     * this is the point at which the customer has
                     * answered and the AI runtime may proceed.
                     */
                    case "in-progress",
                         "answered" ->

                            TelephonyConstants
                                    .EVENT_CALL_ANSWERED;

                    /*
                     * -------------------------------------------------
                     * CALL COMPLETED
                     * -------------------------------------------------
                     */
                    case "completed" ->

                            TelephonyConstants
                                    .EVENT_CALL_COMPLETED;

                    /*
                     * -------------------------------------------------
                     * CALL FAILED
                     * -------------------------------------------------
                     */
                    case "failed" ->

                            TelephonyConstants
                                    .EVENT_CALL_FAILED;

                    /*
                     * -------------------------------------------------
                     * CALL BUSY
                     * -------------------------------------------------
                     */
                    case "busy" ->

                            TelephonyConstants
                                    .EVENT_CALL_BUSY;

                    /*
                     * -------------------------------------------------
                     * CALL NOT ANSWERED
                     * -------------------------------------------------
                     */
                    case "no-answer",
                         "no_answer" ->

                            TelephonyConstants
                                    .EVENT_CALL_NO_ANSWER;

                    /*
                     * -------------------------------------------------
                     * CALL CANCELLED
                     * -------------------------------------------------
                     *
                     * Both spellings are supported because
                     * provider payloads/integrations may use
                     * canceled or cancelled.
                     */
                    case "canceled",
                         "cancelled",
                         "canceled-by-caller",
                         "cancelled-by-caller" ->

                            TelephonyConstants
                                    .EVENT_CALL_CANCELLED;

                    /*
                     * -------------------------------------------------
                     * CALL REJECTED
                     * -------------------------------------------------
                     */
                    case "rejected" ->

                            TelephonyConstants
                                    .EVENT_CALL_REJECTED;

                    /*
                     * -------------------------------------------------
                     * UNKNOWN STATUS
                     * -------------------------------------------------
                     */
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