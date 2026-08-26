package com.infinitio.aivoiceplatform.aidialer.service.impl.scheduler;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.constant.DialerMessages;
import com.infinitio.aivoiceplatform.aidialer.constant.DialerStatus;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.repository.AiDialerRepository;
import com.infinitio.aivoiceplatform.aidialer.repository.DialerCallRepository;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallInitiationService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerSchedulerService;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Scheduler service responsible for processing AI Dialers
 * and initiating outbound campaign calls.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialerSchedulerServiceImpl
        implements DialerSchedulerService {

    private static final Integer NOT_DELETED = 0;

    /*
     * Contact statuses which are eligible for
     * a new outbound attempt.
     */
    private static final Set<String> ELIGIBLE_CONTACT_STATUSES =
            Set.of(
                    "PENDING",
                    "NO_ANSWER",
                    "BUSY",
                    "FAILED"
            );

    /*
     * Call statuses which consume a concurrent
     * call slot.
     */
    private static final Set<CallAttemptStatus> ACTIVE_CALL_STATUSES =
            EnumSet.of(
                    CallAttemptStatus.QUEUED,
                    CallAttemptStatus.DIALING,
                    CallAttemptStatus.RINGING,
                    CallAttemptStatus.ANSWERED,
                    CallAttemptStatus.IN_PROGRESS
            );

    private final AiDialerRepository aiDialerRepository;

    private final DialerCallRepository dialerCallRepository;

    private final CampaignContactRepository
            campaignContactRepository;

    private final DialerCallService dialerCallService;

    private final DialerCallInitiationService
            dialerCallInitiationService;

    // =========================================================
    // PROCESS ONE DIALER
    // =========================================================

    /**
     * Processes one running AI Dialer.
     *
     * @param dialerPublicId AI Dialer public identifier
     * @return calls created during this scheduler cycle
     */
    @Override
    public List<DialerCallResponse> processDialer(
            String dialerPublicId) {

        log.info(
                "Processing AI Dialer : {}",
                dialerPublicId
        );

        AiDialer dialer =
                getDialer(
                        dialerPublicId
                );

        /*
         * Only RUNNING dialers can generate calls.
         */
        if (dialer.getStatus()
                != DialerStatus.RUNNING) {

            log.debug(
                    "Dialer {} is not RUNNING. "
                            + "Current status : {}",
                    dialerPublicId,
                    dialer.getStatus()
            );

            return List.of();
        }

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * Respect scheduled start time.
         */
        if (dialer.getScheduledStartAt() != null
                && now.isBefore(
                dialer.getScheduledStartAt()
        )) {

            log.debug(
                    "Dialer {} has not reached "
                            + "scheduled start time.",
                    dialerPublicId
            );

            return List.of();
        }

        /*
         * Respect scheduled end time.
         *
         * Once the end time is reached, no new calls
         * are generated and the dialer is completed.
         */
        if (dialer.getScheduledEndAt() != null
                && !now.isBefore(
                dialer.getScheduledEndAt()
        )) {

            log.info(
                    "Dialer {} reached scheduled end time.",
                    dialerPublicId
            );

            markDialerCompleted(
                    dialer
            );

            return List.of();
        }

        /*
         * Validate calls per minute configuration.
         */
        if (dialer.getCallsPerMinute() == null
                || dialer.getCallsPerMinute() <= 0) {

            log.warn(
                    "Dialer {} has invalid callsPerMinute.",
                    dialerPublicId
            );

            return List.of();
        }

        /*
         * Validate maximum concurrency.
         */
        if (dialer.getMaxConcurrentCalls() == null
                || dialer.getMaxConcurrentCalls() <= 0) {

            log.warn(
                    "Dialer {} has invalid maxConcurrentCalls.",
                    dialerPublicId
            );

            return List.of();
        }

        /*
         * Count currently active calls.
         */
        int activeCalls =
                countActiveCalls(
                        dialer
                );

        int maxConcurrent =
                dialer.getMaxConcurrentCalls();

        int availableConcurrency =
                Math.max(
                        0,
                        maxConcurrent - activeCalls
                );

        if (availableConcurrency <= 0) {

            log.debug(
                    "Dialer {} reached maximum concurrency. "
                            + "Active : {}, Max : {}",
                    dialerPublicId,
                    activeCalls,
                    maxConcurrent
            );

            return List.of();
        }

        /*
         * callsPerMinute determines the maximum number
         * of contacts to process in this scheduler cycle.
         */
        int numberToDial =
                Math.min(
                        dialer.getCallsPerMinute(),
                        availableConcurrency
                );

        if (numberToDial <= 0) {

            return List.of();
        }

        /*
         * Fetch eligible campaign contacts.
         */
        List<CampaignContact> contacts =
                findEligibleContacts(
                        dialer,
                        numberToDial
                );

        if (contacts.isEmpty()) {

            log.debug(
                    "No eligible contacts found for dialer {}.",
                    dialerPublicId
            );

            return List.of();
        }

        List<DialerCallResponse> createdCalls =
                new ArrayList<>();

        for (CampaignContact contact : contacts) {

            /*
             * Re-check concurrency before each
             * individual call.
             */
            int currentActiveCalls =
                    countActiveCalls(
                            dialer
                    );

            if (currentActiveCalls
                    >= maxConcurrent) {

                log.debug(
                        "Dialer {} reached concurrency "
                                + "while processing contacts.",
                        dialerPublicId
                );

                break;
            }

            try {

                /*
                 * Create the DialerCall in QUEUED state.
                 */
                DialerCallResponse queuedCall =
                        dialerCallService.createCall(
                                dialer.getPublicId(),
                                contact.getPublicId()
                        );

                log.info(
                        "DialerCall {} queued for Contact {}.",
                        queuedCall.getPublicId(),
                        contact.getPublicId()
                );

                try {

                    /*
                     * Immediately initiate the queued call
                     * through the telephony provider.
                     */
                    DialerCallResponse initiatedCall =
                            dialerCallInitiationService
                                    .initiateCall(
                                            queuedCall.getPublicId()
                                    );

                    createdCalls.add(
                            initiatedCall
                    );

                    log.info(
                            "DialerCall initiated successfully. "
                                    + "Dialer : {}, "
                                    + "Contact : {}, "
                                    + "Call : {}",
                            dialer.getPublicId(),
                            contact.getPublicId(),
                            initiatedCall.getPublicId()
                    );

                } catch (Exception exception) {

                    /*
                     * The failure of one call must not stop
                     * the remaining campaign contacts.
                     */
                    log.error(
                            "Unable to initiate DialerCall {} : {}",
                            queuedCall.getPublicId(),
                            exception.getMessage(),
                            exception
                    );
                }

            } catch (Exception exception) {

                /*
                 * The failure of one contact must not stop
                 * the entire scheduler cycle.
                 */
                log.error(
                        "Unable to create DialerCall "
                                + "for contact {} : {}",
                        contact.getPublicId(),
                        exception.getMessage(),
                        exception
                );
            }
        }

        log.info(
                "Dialer {} processed. Created {} calls.",
                dialerPublicId,
                createdCalls.size()
        );

        return createdCalls;
    }

    // =========================================================
    // PROCESS ALL DIALERS
    // =========================================================

    /**
     * Processes scheduled and running AI Dialers.
     */
    @Override
    public void processRunningDialers() {

        log.debug(
                "Starting scheduled AI Dialer processing."
        );

        /*
         * First activate scheduled dialers whose
         * scheduled start time has been reached.
         */
        activateScheduledDialers();

        /*
         * Fetch all currently running dialers.
         */
        List<AiDialer> dialers =
                aiDialerRepository
                        .findAllByStatusAndIsDeleted(
                                DialerStatus.RUNNING,
                                NOT_DELETED
                        );

        if (dialers.isEmpty()) {

            log.debug(
                    "No RUNNING AI Dialers found."
            );

            return;
        }

        for (AiDialer dialer : dialers) {

            try {

                processDialer(
                        dialer.getPublicId()
                );

            } catch (Exception exception) {

                /*
                 * One dialer failure must not stop
                 * processing of other dialers.
                 */
                log.error(
                        "Error processing dialer {} : {}",
                        dialer.getPublicId(),
                        exception.getMessage(),
                        exception
                );
            }
        }
    }

    // =========================================================
    // ACTIVATE SCHEDULED DIALERS
    // =========================================================

    /**
     * Moves scheduled dialers to RUNNING when their
     * configured start time is reached.
     */
    @Transactional
    protected void activateScheduledDialers() {

        List<AiDialer> scheduledDialers =
                aiDialerRepository
                        .findAllByStatusAndIsDeleted(
                                DialerStatus.SCHEDULED,
                                NOT_DELETED
                        );

        if (scheduledDialers.isEmpty()) {

            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        for (AiDialer dialer :
                scheduledDialers) {

            if (dialer.getScheduledStartAt() == null) {

                log.warn(
                        "Scheduled Dialer {} has no "
                                + "scheduled start time.",
                        dialer.getPublicId()
                );

                continue;
            }

            if (now.isBefore(
                    dialer.getScheduledStartAt()
            )) {

                continue;
            }

            /*
             * If the scheduled end time has already
             * passed, complete the dialer instead
             * of starting it.
             */
            if (dialer.getScheduledEndAt() != null
                    && !now.isBefore(
                    dialer.getScheduledEndAt()
            )) {

                markDialerCompleted(
                        dialer
                );

                continue;
            }

            dialer.setStatus(
                    DialerStatus.RUNNING
            );

            aiDialerRepository.save(
                    dialer
            );

            log.info(
                    "Scheduled Dialer {} transitioned "
                            + "from SCHEDULED to RUNNING.",
                    dialer.getPublicId()
            );
        }
    }

    // =========================================================
    // STOP DIALER
    // =========================================================

    /**
     * Stops an AI Dialer.
     *
     * @param dialerPublicId AI Dialer public identifier
     */
    @Override
    @Transactional
    public void stopDialer(
            String dialerPublicId) {

        AiDialer dialer =
                getDialer(
                        dialerPublicId
                );

        if (dialer.getStatus()
                == DialerStatus.STOPPED) {

            return;
        }

        dialer.setStatus(
                DialerStatus.STOPPED
        );

        aiDialerRepository.save(
                dialer
        );

        log.info(
                "Dialer {} stopped by scheduler.",
                dialerPublicId
        );
    }

    // =========================================================
    // SCHEDULED JOB
    // =========================================================

    /**
     * Scheduled AI Dialer processing job.
     */
    @Scheduled(
            fixedRateString =
                    "${ai.dialer.scheduler.interval-ms:60000}"
    )
    public void scheduledDialerProcessing() {

        processRunningDialers();
    }

    // =========================================================
    // FIND ELIGIBLE CONTACTS
    // =========================================================

    /**
     * Finds contacts eligible for a new dialing attempt.
     *
     * @param dialer AI Dialer
     * @param limit maximum contacts
     * @return eligible contacts
     */
    private List<CampaignContact> findEligibleContacts(
            AiDialer dialer,
            int limit) {

        /*
         * Fetch more records than required because
         * some contacts can fail the retry and status
         * eligibility checks.
         */
        Pageable pageable =
                PageRequest.of(
                        0,
                        Math.max(
                                limit * 3,
                                20
                        ),
                        Sort.by(
                                Sort.Direction.DESC,
                                "priority"
                        )
                );

        Page<CampaignContact> page =
                campaignContactRepository
                        .findByCampaignId(
                                dialer
                                        .getCampaign()
                                        .getId(),
                                pageable
                        );

        List<CampaignContact> eligible =
                new ArrayList<>();

        for (CampaignContact contact :
                page.getContent()) {

            if (eligible.size() >= limit) {

                break;
            }

            if (isEligible(
                    contact,
                    dialer
            )) {

                eligible.add(
                        contact
                );
            }
        }

        return eligible;
    }

    // =========================================================
    // CONTACT ELIGIBILITY
    // =========================================================

    /**
     * Determines whether a Campaign Contact is
     * eligible for another outbound attempt.
     *
     * @param contact campaign contact
     * @param dialer AI Dialer
     * @return true when eligible
     */
    private boolean isEligible(
            CampaignContact contact,
            AiDialer dialer) {

        /*
         * Never dial deleted contacts.
         */
        if (contact.getIsDeleted() != null
                && contact.getIsDeleted() == 1) {

            return false;
        }

        /*
         * Never dial inactive contacts.
         */
        if (contact.getIsActive() != null
                && contact.getIsActive() == 0) {

            return false;
        }

        /*
         * Phone number is mandatory.
         */
        if (contact.getPhoneNumber() == null
                || contact.getPhoneNumber().isBlank()) {

            return false;
        }

        /*
         * Contact status must be eligible.
         */
        if (contact.getStatus() == null
                || !ELIGIBLE_CONTACT_STATUSES
                .contains(
                        contact.getStatus()
                                .toUpperCase()
                )) {

            return false;
        }

        /*
         * Explicit DND protection.
         */
        if ("DND_BLOCKED".equalsIgnoreCase(
                contact.getStatus()
        )) {

            return false;
        }

        /*
         * Respect maximum retry/attempt limit.
         */
        int attempts =
                contact.getAttemptCount() == null
                        ? 0
                        : contact.getAttemptCount();

        if (dialer.getMaxRetryAttempts() != null
                && attempts
                >= dialer.getMaxRetryAttempts()) {

            return false;
        }

        /*
         * Respect retry delay.
         */
        return isRetryDelayCompleted(
                contact,
                dialer
        );
    }

    // =========================================================
    // RETRY DELAY
    // =========================================================

    /**
     * Checks whether the configured retry delay
     * has elapsed.
     *
     * @param contact campaign contact
     * @param dialer AI Dialer
     * @return true when retry is allowed
     */
    private boolean isRetryDelayCompleted(
            CampaignContact contact,
            AiDialer dialer) {

        /*
         * First attempt.
         */
        if (contact.getLastAttemptAt() == null) {

            return true;
        }

        /*
         * No retry delay configured.
         */
        if (dialer.getRetryDelaySeconds() == null
                || dialer.getRetryDelaySeconds() <= 0) {

            return true;
        }

        LocalDateTime nextAttemptAt =
                contact.getLastAttemptAt()
                        .plusSeconds(
                                dialer.getRetryDelaySeconds()
                        );

        return !LocalDateTime.now()
                .isBefore(
                        nextAttemptAt
                );
    }

    // =========================================================
    // ACTIVE CALL COUNT
    // =========================================================

    /**
     * Counts active calls for an AI Dialer.
     *
     * @param dialer AI Dialer
     * @return number of active calls
     */
    private int countActiveCalls(
            AiDialer dialer) {

        List<DialerCall> calls =
                dialerCallRepository
                        .findAllByDialerIdAndIsDeleted(
                                dialer.getId(),
                                NOT_DELETED
                        );

        int count = 0;

        for (DialerCall call :
                calls) {

            if (call.getStatus() != null
                    && ACTIVE_CALL_STATUSES
                    .contains(
                            call.getStatus()
                    )) {

                count++;
            }
        }

        return count;
    }

    // =========================================================
    // MARK DIALER COMPLETED
    // =========================================================

    /**
     * Marks an AI Dialer as completed.
     *
     * @param dialer AI Dialer
     */
    @Transactional
    protected void markDialerCompleted(
            AiDialer dialer) {

        if (dialer.getStatus()
                == DialerStatus.COMPLETED) {

            return;
        }

        dialer.setStatus(
                DialerStatus.COMPLETED
        );

        dialer.setCompletedAt(
                LocalDateTime.now()
        );

        aiDialerRepository.save(
                dialer
        );

        log.info(
                "Dialer {} marked COMPLETED.",
                dialer.getPublicId()
        );
    }

    // =========================================================
    // GET DIALER
    // =========================================================

    /**
     * Gets an AI Dialer by public ID.
     *
     * @param publicId AI Dialer public identifier
     * @return AI Dialer
     */
    private AiDialer getDialer(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new IllegalArgumentException(
                    DialerMessages.DIALER_PUBLIC_ID_REQUIRED
            );
        }

        return aiDialerRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                DialerMessages.DIALER_NOT_FOUND
                        )
                );
    }
}