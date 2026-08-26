package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.repository.DialerCallRepository;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for Dialer Call retry
 * and attempt management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DialerCallRetryServiceImpl
        implements DialerCallRetryService {

    private static final Integer NOT_DELETED = 0;

    private final DialerCallRepository
            dialerCallRepository;

    /**
     * Determines whether another call attempt
     * can be created for the campaign contact.
     *
     * @param dialer AI Dialer
     * @param campaignContactId campaign contact ID
     * @return true when another attempt is allowed
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canRetry(
            AiDialer dialer,
            Long campaignContactId) {

        List<DialerCall> attempts =
                getAttempts(
                        campaignContactId
                );

        /*
         * First attempt is always allowed.
         */
        if (attempts.isEmpty()) {

            return true;
        }

        /*
         * Do not create another call while an
         * existing call is still active.
         */
        if (hasActiveAttempt(
                attempts
        )) {

            return false;
        }

        Integer maxRetryAttempts =
                dialer.getMaxRetryAttempts();

        /*
         * If retry limit is not configured,
         * preserve existing behavior and allow
         * another attempt.
         */
        if (maxRetryAttempts == null) {

            return true;
        }

        return attempts.size()
                < maxRetryAttempts;
    }

    /**
     * Calculates the next attempt number.
     *
     * @param previousCalls previous call attempts
     * @return next attempt number
     */
    @Override
    public int calculateNextAttemptNumber(
            List<DialerCall> previousCalls) {

        return previousCalls.stream()
                .map(
                        DialerCall::getAttemptNumber
                )
                .filter(
                        attemptNumber ->
                                attemptNumber != null
                )
                .max(
                        Integer::compareTo
                )
                .orElse(0)
                + 1;
    }

    /**
     * Checks whether an active attempt already exists.
     *
     * @param campaignContactId campaign contact ID
     * @return true if an active call exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveAttempt(
            Long campaignContactId) {

        return hasActiveAttempt(
                getAttempts(
                        campaignContactId
                )
        );
    }

    /**
     * Gets all attempts for a campaign contact.
     */
    private List<DialerCall> getAttempts(
            Long campaignContactId) {

        return dialerCallRepository
                .findAllByCampaignContactIdAndIsDeleted(
                        campaignContactId,
                        NOT_DELETED
                );
    }

    /**
     * Checks active call statuses.
     */
    private boolean hasActiveAttempt(
            List<DialerCall> attempts) {

        return attempts.stream()
                .anyMatch(
                        attempt ->
                                isActiveStatus(
                                        attempt.getStatus()
                                )
                );
    }

    /**
     * Determines whether a status represents
     * an active call attempt.
     */
    private boolean isActiveStatus(
            CallAttemptStatus status) {

        if (status == null) {

            return false;
        }

        return status == CallAttemptStatus.QUEUED
                || status == CallAttemptStatus.DIALING
                || status == CallAttemptStatus.RINGING
                || status == CallAttemptStatus.ANSWERED
                || status == CallAttemptStatus.IN_PROGRESS;
    }
}