package com.infinitio.aivoiceplatform.telephony.inbound.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.telephony.inbound.constants.InboundTelephonyConstants;
import com.infinitio.aivoiceplatform.telephony.inbound.service.InboundCallStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Default implementation of inbound call status management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InboundCallStatusServiceImpl
        implements InboundCallStatusService {

    private final CallRepository callRepository;

    /**
     * Marks an inbound call as in progress.
     *
     * <p>
     * The first media event represents the point at which
     * the inbound call has started active media processing.
     * </p>
     *
     * @param call call being processed
     * @return updated call
     */
    @Override
    public Call markInProgress(Call call) {

        validateCall(call);

        if (isTerminalStatus(call.getStatus())) {

            log.warn(
                    "Ignoring IN_PROGRESS update for terminal " +
                            "inbound call. callPublicId={}, status={}",
                    call.getPublicId(),
                    call.getStatus()
            );

            return call;
        }

        if (InboundTelephonyConstants.CALL_STATUS_IN_PROGRESS
                .equals(call.getStatus())) {

            return call;
        }

        LocalDateTime answeredAt =
                LocalDateTime.now();

        call.setStatus(
                InboundTelephonyConstants.CALL_STATUS_IN_PROGRESS
        );

        call.setAnsweredAt(
                answeredAt
        );

        /*
         * startedAt is normally populated during inbound Call
         * creation. Keep this fallback for data consistency.
         */
        if (call.getStartedAt() == null) {

            call.setStartedAt(
                    answeredAt
            );
        }

        Call updatedCall =
                callRepository.save(call);

        log.info(
                "Inbound Call marked IN_PROGRESS. " +
                        "callPublicId={}, answeredAt={}",
                updatedCall.getPublicId(),
                answeredAt
        );

        return updatedCall;
    }

    /**
     * Marks an inbound call as completed.
     *
     * @param call call that has ended normally
     * @return updated call
     */
    @Override
    public Call markCompleted(Call call) {

        validateCall(call);

        if (InboundTelephonyConstants.CALL_STATUS_COMPLETED
                .equals(call.getStatus())) {

            return call;
        }

        if (InboundTelephonyConstants.CALL_STATUS_FAILED
                .equals(call.getStatus())) {

            log.warn(
                    "Ignoring COMPLETED update for failed " +
                            "inbound call. callPublicId={}",
                    call.getPublicId()
            );

            return call;
        }

        LocalDateTime endedAt =
                LocalDateTime.now();

        call.setStatus(
                InboundTelephonyConstants.CALL_STATUS_COMPLETED
        );

        call.setEndedAt(
                endedAt
        );

        updateDuration(
                call,
                endedAt
        );

        Call updatedCall =
                callRepository.save(call);

        log.info(
                "Inbound Call marked COMPLETED. " +
                        "callPublicId={}, endedAt={}, " +
                        "durationSeconds={}",
                updatedCall.getPublicId(),
                endedAt,
                updatedCall.getDurationSeconds()
        );

        return updatedCall;
    }

    /**
     * Marks an inbound call as failed.
     *
     * @param call call that failed
     * @return updated call
     */
    @Override
    public Call markFailed(Call call) {

        validateCall(call);

        if (InboundTelephonyConstants.CALL_STATUS_FAILED
                .equals(call.getStatus())) {

            return call;
        }

        if (InboundTelephonyConstants.CALL_STATUS_COMPLETED
                .equals(call.getStatus())) {

            log.warn(
                    "Ignoring FAILED update for completed " +
                            "inbound call. callPublicId={}",
                    call.getPublicId()
            );

            return call;
        }

        LocalDateTime endedAt =
                LocalDateTime.now();

        call.setStatus(
                InboundTelephonyConstants.CALL_STATUS_FAILED
        );

        call.setEndedAt(
                endedAt
        );

        updateDuration(
                call,
                endedAt
        );

        Call updatedCall =
                callRepository.save(call);

        log.info(
                "Inbound Call marked FAILED. " +
                        "callPublicId={}, endedAt={}, " +
                        "durationSeconds={}",
                updatedCall.getPublicId(),
                endedAt,
                updatedCall.getDurationSeconds()
        );

        return updatedCall;
    }

    /**
     * Updates call duration using start and end timestamps.
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
     * Validates the supplied Call.
     *
     * @param call call to validate
     */
    private void validateCall(Call call) {

        if (call == null) {

            throw new IllegalArgumentException(
                    "Call is required."
            );
        }
    }

    /**
     * Checks whether the call is already in a terminal state.
     *
     * @param status current call status
     * @return true when the status is terminal
     */
    private boolean isTerminalStatus(
            String status) {

        return InboundTelephonyConstants.CALL_STATUS_COMPLETED
                .equals(status)
                || InboundTelephonyConstants.CALL_STATUS_FAILED
                .equals(status);
    }
}