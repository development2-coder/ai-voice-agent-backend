package com.infinitio.aivoiceplatform.telephony.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingService;
import com.infinitio.aivoiceplatform.telephony.constant.TelephonyConstants;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyCallStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Implementation responsible for Call lifecycle state changes.
 *
 * <p>
 * Provider-specific events must already be normalized before
 * reaching this service.
 * </p>
 *
 * <p>
 * Recording information received through a telephony webhook
 * is forwarded to CallRecordingService after the Call entity
 * is updated.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelephonyCallStateServiceImpl
        implements TelephonyCallStateService {

    private static final String OUTBOUND =
            "OUTBOUND";

    private static final String INITIATED =
            "INITIATED";

    private final CallRecordingService
            callRecordingService;

    /**
     * Initializes a Call before the provider request.
     *
     * @param call call entity
     * @param providerCode provider code
     * @param fromNumber caller number
     * @param toNumber destination number
     */
    @Override
    public void initializeOutboundCall(
            Call call,
            String providerCode,
            String fromNumber,
            String toNumber) {

        if (call == null) {

            log.warn(
                    "Cannot initialize null Call."
            );

            return;
        }

        call.setProvider(
                providerCode
        );

        call.setFromNumber(
                fromNumber
        );

        call.setToNumber(
                toNumber
        );

        call.setDirection(
                OUTBOUND
        );

        call.setStatus(
                INITIATED
        );

        call.setStartedAt(
                LocalDateTime.now()
        );

        log.info(
                "Call initialized for outbound telephony. "
                        + "callPublicId={}, provider={}, "
                        + "from={}, to={}",
                call.getPublicId(),
                providerCode,
                fromNumber,
                toNumber
        );
    }

    /**
     * Updates a Call using the provider response received
     * immediately after the outbound request.
     *
     * @param call call entity
     * @param response provider response
     */
    @Override
    public void updateFromProviderResponse(
            Call call,
            ProviderCallResponseDto response) {

        if (call == null
                || response == null) {

            return;
        }

        if (response.getProviderCallId() != null
                && !response
                .getProviderCallId()
                .isBlank()) {

            call.setProviderCallId(
                    response.getProviderCallId()
            );
        }

        if (response.getStatus() != null
                && !response
                .getStatus()
                .isBlank()) {

            call.setStatus(
                    response.getStatus()
            );
        }

        log.info(
                "Call updated from provider response. "
                        + "callPublicId={}, providerCallId={}, "
                        + "providerStatus={}",
                call.getPublicId(),
                call.getProviderCallId(),
                response.getStatus()
        );
    }

    /**
     * Updates Call lifecycle state from a normalized
     * provider webhook event.
     *
     * @param call call entity
     * @param event normalized provider event
     */
    @Override
    public void updateFromWebhookEvent(
            Call call,
            NormalizedCallEventDto event) {

        if (call == null
                || event == null) {

            log.warn(
                    "Cannot update Call lifecycle because "
                            + "Call or event is null."
            );

            return;
        }

        String eventName =
                event.getEvent();

        log.info(
                "Updating Call from telephony webhook. "
                        + "callPublicId={}, providerCallId={}, event={}",
                call.getPublicId(),
                call.getProviderCallId(),
                eventName
        );

        updateProviderInformation(
                call,
                event
        );

        /*
         * ---------------------------------------------------------
         * CALL INITIATED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_INITIATED
                .equalsIgnoreCase(
                        eventName
                )) {

            call.setStatus(
                    TelephonyConstants
                            .EVENT_CALL_INITIATED
            );

            if (call.getStartedAt() == null) {

                call.setStartedAt(
                        toLocalDateTime(
                                event.getTimestamp()
                        )
                );
            }

            log.debug(
                    "Call marked as initiated. "
                            + "callPublicId={}",
                    call.getPublicId()
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL RINGING
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_RINGING
                .equalsIgnoreCase(
                        eventName
                )) {

            call.setStatus(
                    TelephonyConstants
                            .EVENT_CALL_RINGING
            );

            if (call.getStartedAt() == null) {

                call.setStartedAt(
                        toLocalDateTime(
                                event.getTimestamp()
                        )
                );
            }

            log.debug(
                    "Call marked as ringing. "
                            + "callPublicId={}",
                    call.getPublicId()
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL ANSWERED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_ANSWERED
                .equalsIgnoreCase(
                        eventName
                )) {

            call.setStatus(
                    TelephonyConstants
                            .EVENT_CALL_ANSWERED
            );

            LocalDateTime answeredAt =
                    toLocalDateTime(
                            event.getTimestamp()
                    );

            call.setAnsweredAt(
                    answeredAt
            );

            if (call.getStartedAt() == null) {

                call.setStartedAt(
                        answeredAt
                );
            }

            log.info(
                    "Call marked as answered. "
                            + "callPublicId={}, answeredAt={}",
                    call.getPublicId(),
                    answeredAt
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL COMPLETED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_COMPLETED
                .equalsIgnoreCase(
                        eventName
                )
                || TelephonyConstants.EVENT_CALL_ENDED
                .equalsIgnoreCase(
                        eventName
                )) {

            call.setStatus(
                    TelephonyConstants
                            .EVENT_CALL_COMPLETED
            );

            LocalDateTime endedAt =
                    toLocalDateTime(
                            event.getTimestamp()
                    );

            call.setEndedAt(
                    endedAt
            );

            updateDuration(
                    call,
                    endedAt
            );

            /*
             * Store recording information before returning
             * from the completed-call branch.
             */
            processRecording(
                    call,
                    event
            );

            log.info(
                    "Call marked as completed. "
                            + "callPublicId={}, endedAt={}, "
                            + "durationSeconds={}",
                    call.getPublicId(),
                    endedAt,
                    call.getDurationSeconds()
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL FAILED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_FAILED
                .equalsIgnoreCase(
                        eventName
                )) {

            markFailed(
                    call,
                    "Telephony provider reported call failure."
            );

            /*
             * A provider may send recording information together
             * with a terminal event. Process it independently.
             */
            processRecording(
                    call,
                    event
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL BUSY
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_BUSY
                .equalsIgnoreCase(
                        eventName
                )) {

            markTerminalStatus(
                    call,
                    TelephonyConstants.EVENT_CALL_BUSY
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL NO ANSWER
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_NO_ANSWER
                .equalsIgnoreCase(
                        eventName
                )) {

            markTerminalStatus(
                    call,
                    TelephonyConstants.EVENT_CALL_NO_ANSWER
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL CANCELLED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_CANCELLED
                .equalsIgnoreCase(
                        eventName
                )) {

            markTerminalStatus(
                    call,
                    TelephonyConstants.EVENT_CALL_CANCELLED
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL REJECTED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_REJECTED
                .equalsIgnoreCase(
                        eventName
                )) {

            markTerminalStatus(
                    call,
                    TelephonyConstants.EVENT_CALL_REJECTED
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * RECORDING-ONLY EVENT
         * ---------------------------------------------------------
         *
         * Some providers send the recording callback separately
         * from call.completed.
         */
        if (event.getRecordingUrl() != null
                && !event
                .getRecordingUrl()
                .isBlank()) {

            processRecording(
                    call,
                    event
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * UNKNOWN EVENT
         * ---------------------------------------------------------
         */
        log.warn(
                "Unknown normalized telephony event. "
                        + "callPublicId={}, event={}",
                call.getPublicId(),
                eventName
        );
    }

    /**
     * Updates basic provider information from the webhook.
     *
     * @param call call entity
     * @param event normalized event
     */
    private void updateProviderInformation(
            Call call,
            NormalizedCallEventDto event) {

        if (event.getProviderCallId() != null
                && !event
                .getProviderCallId()
                .isBlank()) {

            call.setProviderCallId(
                    event.getProviderCallId()
            );
        }

        if (event.getProvider() != null
                && !event
                .getProvider()
                .isBlank()) {

            call.setProvider(
                    event.getProvider()
            );
        }

        if (event.getFromNumber() != null
                && !event
                .getFromNumber()
                .isBlank()) {

            call.setFromNumber(
                    event.getFromNumber()
            );
        }

        if (event.getToNumber() != null
                && !event
                .getToNumber()
                .isBlank()) {

            call.setToNumber(
                    event.getToNumber()
            );
        }
    }

    /**
     * Processes recording information received from the provider.
     *
     * <p>
     * Recording persistence is intentionally isolated from the
     * Call lifecycle. A recording failure must not cause a
     * successfully completed call to become failed.
     * </p>
     *
     * @param call call entity
     * @param event normalized provider event
     */
    private void processRecording(
            Call call,
            NormalizedCallEventDto event) {

        if (event.getRecordingUrl() == null
                || event
                .getRecordingUrl()
                .isBlank()) {

            return;
        }

        call.setRecordingUrl(
                event.getRecordingUrl()
        );

        log.info(
                "Recording URL received. "
                        + "callPublicId={}, provider={}, "
                        + "recordingUrl={}, durationSeconds={}",
                call.getPublicId(),
                event.getProvider(),
                event.getRecordingUrl(),
                event.getRecordingDurationSeconds()
        );

        try {

            callRecordingService.createFromWebhook(
                    call.getPublicId(),
                    event.getRecordingUrl(),
                    event.getRecordingDurationSeconds(),
                    event.getProvider()
            );

            log.info(
                    "CallRecording processed successfully. "
                            + "callPublicId={}, recordingUrl={}",
                    call.getPublicId(),
                    event.getRecordingUrl()
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to create CallRecording from "
                            + "telephony webhook. "
                            + "callPublicId={}, recordingUrl={}, "
                            + "reason={}",
                    call.getPublicId(),
                    event.getRecordingUrl(),
                    exception.getMessage(),
                    exception
            );
        }
    }

    /**
     * Marks a Call as failed.
     *
     * @param call call entity
     * @param reason failure reason
     */
    @Override
    public void markFailed(
            Call call,
            String reason) {

        if (call == null) {

            return;
        }

        call.setStatus(
                TelephonyConstants
                        .EVENT_CALL_FAILED
        );

        call.setFailureReason(
                truncate(
                        reason,
                        500
                )
        );

        LocalDateTime endedAt =
                LocalDateTime.now();

        call.setEndedAt(
                endedAt
        );

        updateDuration(
                call,
                endedAt
        );

        log.warn(
                "Call marked as failed. "
                        + "callPublicId={}, reason={}",
                call.getPublicId(),
                reason
        );
    }

    /**
     * Marks a Call with a terminal provider event.
     *
     * @param call call entity
     * @param status normalized terminal status
     */
    private void markTerminalStatus(
            Call call,
            String status) {

        call.setStatus(
                status
        );

        LocalDateTime endedAt =
                LocalDateTime.now();

        call.setEndedAt(
                endedAt
        );

        updateDuration(
                call,
                endedAt
        );

        log.info(
                "Call marked with terminal status. "
                        + "callPublicId={}, status={}, endedAt={}",
                call.getPublicId(),
                status,
                endedAt
        );
    }

    /**
     * Updates duration using Call start and end timestamps.
     *
     * @param call call entity
     * @param endedAt call end timestamp
     */
    private void updateDuration(
            Call call,
            LocalDateTime endedAt) {

        if (call.getStartedAt() == null
                || endedAt == null) {

            return;
        }

        long durationSeconds =
                Duration.between(
                        call.getStartedAt(),
                        endedAt
                ).getSeconds();

        call.setDurationSeconds(
                (int) Math.max(
                        durationSeconds,
                        0
                )
        );
    }

    /**
     * Converts provider timestamp to LocalDateTime.
     *
     * @param timestamp provider timestamp
     * @return local date time in UTC
     */
    private LocalDateTime toLocalDateTime(
            Instant timestamp) {

        if (timestamp == null) {

            return LocalDateTime.now();
        }

        return LocalDateTime.ofInstant(
                timestamp,
                ZoneOffset.UTC
        );
    }

    /**
     * Limits a string to the requested maximum length.
     *
     * @param value value
     * @param maxLength maximum length
     * @return truncated value
     */
    private String truncate(
            String value,
            int maxLength) {

        if (value == null) {

            return null;
        }

        if (value.length() <= maxLength) {

            return value;
        }

        return value.substring(
                0,
                maxLength
        );
    }
}