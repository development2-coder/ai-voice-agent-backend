package com.infinitio.aivoiceplatform.callrecording.service.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.callrecording.dto.response.CallRecordingResponse;
import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingRecoveryService;
import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingService;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.provider.TelephonyProvider;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptService;
import java.time.LocalDateTime;
import com.infinitio.aivoiceplatform.telephony.constants.TelephonyConstants;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation for recovering call recordings
 * from the telephony provider when the normal callback
 * is not received.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CallRecordingRecoveryServiceImpl
        implements CallRecordingRecoveryService {

    /**
     * Initial delay before querying the provider.
     */
    private static final long INITIAL_DELAY_SECONDS = 10L;

    /**
     * Maximum number of recovery attempts.
     */
    private static final int MAX_ATTEMPTS = 10;

    /**
     * Delay between subsequent recovery attempts.
     */
    private static final long RETRY_DELAY_SECONDS = 30L;

    private final TelephonyProvider telephonyProvider;

    private final CallRecordingService callRecordingService;

    private final TranscriptService transcriptService;

    private final ScheduledExecutorService
            scheduledExecutorService =
            Executors.newScheduledThreadPool(2);

    private final CallRepository callRepository;

    /**
     * Keeps track of recovery operations which are already
     * scheduled or running for a call.
     */
    private final Set<String> recoveryInProgress =
            ConcurrentHashMap.newKeySet();

    /**
     * Schedules recording recovery.
     *
     * @param callPublicId platform call public identifier
     * @param providerCallId provider call identifier
     */
    @Override
    public void recoverRecording(
            String callPublicId,
            String providerCallId) {

        /*
         * Validate call public ID before creating recovery key.
         */
        if (callPublicId == null
                || callPublicId.isBlank()) {

            log.warn(
                    "Recording recovery skipped because "
                            + "callPublicId is missing."
            );

            return;
        }

        /*
         * Validate provider call ID.
         */
        if (providerCallId == null
                || providerCallId.isBlank()) {

            log.warn(
                    "Recording recovery skipped because "
                            + "providerCallId is missing. "
                            + "callPublicId={}",
                    callPublicId
            );

            return;
        }

        /*
         * Create a unique key for this recovery operation.
         */
        String recoveryKey =
                callPublicId + ":" + providerCallId;

        /*
         * Prevent duplicate recovery scheduling for the same call.
         *
         * This is important because recovery can be triggered
         * from more than one WebSocket lifecycle event.
         */
        if (!recoveryInProgress.add(recoveryKey)) {

            log.info(
                    "Recording recovery already scheduled. "
                            + "callPublicId={}, providerCallId={}",
                    callPublicId,
                    providerCallId
            );

            return;
        }

        log.info(
                "Scheduling Exotel recording recovery. "
                        + "callPublicId={}, providerCallId={}, "
                        + "initialDelaySeconds={}",
                callPublicId,
                providerCallId,
                INITIAL_DELAY_SECONDS
        );

        try {

            scheduledExecutorService.schedule(
                    () -> executeRecovery(
                            callPublicId,
                            providerCallId,
                            1,
                            recoveryKey
                    ),
                    INITIAL_DELAY_SECONDS,
                    TimeUnit.SECONDS
            );

        } catch (Exception exception) {

            /*
             * If scheduling itself fails, remove the key so that
             * another recovery attempt can be scheduled later.
             */
            recoveryInProgress.remove(
                    recoveryKey
            );

            log.error(
                    "Unable to schedule Exotel recording recovery. "
                            + "callPublicId={}, providerCallId={}",
                    callPublicId,
                    providerCallId,
                    exception
            );
        }
    }

    /**
     * Executes one recording recovery attempt.
     *
     * @param callPublicId platform call public identifier
     * @param providerCallId provider call identifier
     * @param attempt current attempt number
     * @param recoveryKey unique recovery key
     */
    private void executeRecovery(
            String callPublicId,
            String providerCallId,
            int attempt,
            String recoveryKey) {

        try {

            log.info(
                    "Starting call recording recovery. "
                            + "callPublicId={}, providerCallId={}, "
                            + "attempt={}/{}",
                    callPublicId,
                    providerCallId,
                    attempt,
                    MAX_ATTEMPTS
            );

            NormalizedCallEventDto callDetails =
                    telephonyProvider.getCallDetails(
                            providerCallId
                    );

            if (callDetails == null) {

                log.warn(
                        "Provider returned no call details. "
                                + "callPublicId={}, providerCallId={}, "
                                + "attempt={}",
                        callPublicId,
                        providerCallId,
                        attempt
                );

                scheduleRetryIfRequired(
                        callPublicId,
                        providerCallId,
                        attempt,
                        recoveryKey
                );

                return;
            }

            updateCallStatusFromRecovery(
                    callPublicId,
                    callDetails
            );

            String recordingUrl =
                    callDetails.getRecordingUrl();

            if (recordingUrl == null
                    || recordingUrl.isBlank()) {

                log.warn(
                        "Recording URL is not available yet. "
                                + "callPublicId={}, providerCallId={}, "
                                + "status={}, attempt={}/{}",
                        callPublicId,
                        providerCallId,
                        callDetails.getEvent(),
                        attempt,
                        MAX_ATTEMPTS
                );

                scheduleRetryIfRequired(
                        callPublicId,
                        providerCallId,
                        attempt,
                        recoveryKey
                );

                return;
            }

            log.info(
                    "Recording URL found through Exotel Call Details API. "
                            + "callPublicId={}, providerCallId={}",
                    callPublicId,
                    providerCallId
            );

            /*
             * Existing recording creation flow.
             */
            CallRecordingResponse recording =
                    callRecordingService.createFromWebhook(
                            callPublicId,
                            recordingUrl,
                            callDetails.getRecordingDurationSeconds(),
                            callDetails.getProvider()
                    );

            String recordingPublicId =
                    recording != null
                            ? recording.getPublicId()
                            : null;

            /*
             * Update the Call entity with the recovered recording URL.
             *
             * This is required so the Calls API can return the
             * recording URL to the frontend after recovery.
             */
            Call call =
                    callRepository
                            .findByPublicId(callPublicId)
                            .orElse(null);

            if (call != null) {

                call.setRecordingUrl(
                        recordingUrl
                );

                if (callDetails.getRecordingDurationSeconds() != null) {

                    call.setDurationSeconds(
                            callDetails.getRecordingDurationSeconds()
                    );
                }

                callRepository.save(
                        call
                );

                log.info(
                        "Call recording URL updated after recovery. "
                                + "callPublicId={}, recordingUrl={}, "
                                + "durationSeconds={}",
                        callPublicId,
                        recordingUrl,
                        callDetails.getRecordingDurationSeconds()
                );

            } else {

                log.warn(
                        "Call not found while updating recovered recording URL. "
                                + "callPublicId={}",
                        callPublicId
                );
            }

            log.info(
                    "Call recording recovered successfully. "
                            + "callPublicId={}, providerCallId={}, "
                            + "recordingPublicId={}",
                    callPublicId,
                    providerCallId,
                    recordingPublicId
            );

            /*
             * Existing transcript finalization flow.
             */
            finalizeTranscript(
                    callPublicId,
                    recordingPublicId
            );

            /*
             * Recovery completed successfully.
             * Remove the key so future recovery requests are allowed
             * if they are genuinely required.
             */
            recoveryInProgress.remove(
                    recoveryKey
            );

        } catch (Exception exception) {

            log.error(
                    "Call recording recovery failed. "
                            + "callPublicId={}, providerCallId={}, "
                            + "attempt={}/{}",
                    callPublicId,
                    providerCallId,
                    attempt,
                    MAX_ATTEMPTS,
                    exception
            );

            scheduleRetryIfRequired(
                    callPublicId,
                    providerCallId,
                    attempt,
                    recoveryKey
            );
        }
    }

    /**
     * Schedules another recovery attempt when the maximum
     * number of attempts has not been reached.
     *
     * @param callPublicId platform call public identifier
     * @param providerCallId provider call identifier
     * @param attempt current attempt number
     * @param recoveryKey unique recovery key
     */
    private void scheduleRetryIfRequired(
            String callPublicId,
            String providerCallId,
            int attempt,
            String recoveryKey) {

        if (attempt >= MAX_ATTEMPTS) {

            log.error(
                    "Call recording recovery exhausted all attempts. "
                            + "callPublicId={}, providerCallId={}, "
                            + "attempts={}",
                    callPublicId,
                    providerCallId,
                    MAX_ATTEMPTS
            );

            /*
             * Recovery is finished because no more retries
             * will be scheduled.
             */
            recoveryInProgress.remove(
                    recoveryKey
            );

            return;
        }

        int nextAttempt =
                attempt + 1;

        log.info(
                "Scheduling call recording recovery retry. "
                        + "callPublicId={}, providerCallId={}, "
                        + "nextAttempt={}, delaySeconds={}",
                callPublicId,
                providerCallId,
                nextAttempt,
                RETRY_DELAY_SECONDS
        );

        try {

            scheduledExecutorService.schedule(
                    () -> executeRecovery(
                            callPublicId,
                            providerCallId,
                            nextAttempt,
                            recoveryKey
                    ),
                    RETRY_DELAY_SECONDS,
                    TimeUnit.SECONDS
            );

        } catch (Exception exception) {

            /*
             * If retry scheduling fails, remove the recovery key
             * because there will be no further recovery execution.
             */
            recoveryInProgress.remove(
                    recoveryKey
            );

            log.error(
                    "Unable to schedule call recording recovery retry. "
                            + "callPublicId={}, providerCallId={}, "
                            + "nextAttempt={}",
                    callPublicId,
                    providerCallId,
                    nextAttempt,
                    exception
            );
        }
    }

    /**
     * Updates the platform Call state using the recovered
     * Exotel Call Details response.
     *
     * <p>
     * This is required when the normal Exotel terminal
     * status callback is not received and the WebSocket
     * closes abnormally.
     * </p>
     *
     * @param callPublicId platform call public identifier
     * @param callDetails recovered provider call details
     */
    private void updateCallFromRecoveredDetails(
            String callPublicId,
            NormalizedCallEventDto callDetails) {

        if (callDetails == null) {
            return;
        }

        if (!TelephonyConstants.EVENT_CALL_COMPLETED
                .equalsIgnoreCase(
                        callDetails.getEvent()
                )
                && !TelephonyConstants.EVENT_CALL_ENDED
                .equalsIgnoreCase(
                        callDetails.getEvent()
                )) {

            return;
        }

        Call call =
                callRepository
                        .findByPublicId(
                                callPublicId
                        )
                        .orElse(null);

        if (call == null) {

            log.warn(
                    "Call not found while updating recovered "
                            + "call status. callPublicId={}",
                    callPublicId
            );

            return;
        }

        call.setStatus(
                TelephonyConstants.EVENT_CALL_COMPLETED
        );

        if (callDetails.getTimestamp() != null) {

            call.setEndedAt(
                    LocalDateTime.ofInstant(
                            callDetails.getTimestamp(),
                            java.time.ZoneId.systemDefault()
                    )
            );

        } else if (call.getEndedAt() == null) {

            call.setEndedAt(
                    LocalDateTime.now()
            );
        }

        if (callDetails.getRecordingDurationSeconds() != null) {

            call.setDurationSeconds(
                    callDetails.getRecordingDurationSeconds()
            );
        }

        callRepository.save(
                call
        );

        log.info(
                "Call status updated from recovered Exotel "
                        + "call details. callPublicId={}, status={}, "
                        + "endedAt={}, durationSeconds={}",
                callPublicId,
                call.getStatus(),
                call.getEndedAt(),
                call.getDurationSeconds()
        );
    }

    /**
     * Updates the platform Call entity using the status
     * recovered from Exotel Call Details API.
     *
     * @param callPublicId platform call public identifier
     * @param callDetails recovered Exotel call details
     */
    private void updateCallStatusFromRecovery(
            String callPublicId,
            NormalizedCallEventDto callDetails) {

        if (callDetails == null) {
            return;
        }

        if (!"COMPLETED".equalsIgnoreCase(
                callDetails.getEvent()
        )
                && !"ENDED".equalsIgnoreCase(
                callDetails.getEvent()
        )) {

            return;
        }

        Call call =
                callRepository
                        .findByPublicId(
                                callPublicId
                        )
                        .orElse(null);

        if (call == null) {

            log.warn(
                    "Call not found while updating recovered "
                            + "call status. callPublicId={}",
                    callPublicId
            );

            return;
        }

        call.setStatus(
                "COMPLETED"
        );

        if (callDetails.getTimestamp() != null) {

            call.setEndedAt(
                    LocalDateTime.ofInstant(
                            callDetails.getTimestamp(),
                            java.time.ZoneId.systemDefault()
                    )
            );
        }

        if (callDetails.getRecordingDurationSeconds() != null) {

            call.setDurationSeconds(
                    callDetails
                            .getRecordingDurationSeconds()
            );
        }

        callRepository.save(
                call
        );

        log.info(
                "Call status updated from recovered Exotel "
                        + "details. callPublicId={}, status={}, "
                        + "endedAt={}, durationSeconds={}",
                callPublicId,
                call.getStatus(),
                call.getEndedAt(),
                call.getDurationSeconds()
        );
    }

    /**
     * Finalizes the transcript after the recording has
     * been successfully recovered.
     *
     * @param callPublicId platform call public identifier
     * @param recordingPublicId recovered recording public ID
     */
    private void finalizeTranscript(
            String callPublicId,
            String recordingPublicId) {

        try {

            transcriptService.finalizeCallTranscript(
                    callPublicId,
                    recordingPublicId
            );

            log.info(
                    "Transcript finalized after recording recovery. "
                            + "callPublicId={}, recordingPublicId={}",
                    callPublicId,
                    recordingPublicId
            );

        } catch (Exception exception) {

            /*
             * Recording recovery itself is already successful.
             * Transcript failure should not cause the recording
             * recovery to be retried.
             */
            log.error(
                    "Transcript finalization failed after "
                            + "recording recovery. "
                            + "callPublicId={}, recordingPublicId={}",
                    callPublicId,
                    recordingPublicId,
                    exception
            );
        }
    }

    /**
     * Shuts down the recovery scheduler when the application
     * is stopped.
     */
    @PreDestroy
    public void shutdown() {

        log.info(
                "Shutting down call recording recovery scheduler."
        );

        scheduledExecutorService.shutdown();

        try {

            if (!scheduledExecutorService.awaitTermination(
                    5,
                    TimeUnit.SECONDS
            )) {

                scheduledExecutorService.shutdownNow();
            }

        } catch (InterruptedException exception) {

            scheduledExecutorService.shutdownNow();

            Thread.currentThread().interrupt();

            log.warn(
                    "Interrupted while shutting down "
                            + "call recording recovery scheduler.",
                    exception
            );
        }
    }
}