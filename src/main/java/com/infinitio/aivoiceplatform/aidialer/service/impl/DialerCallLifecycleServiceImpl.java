package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.repository.DialerCallRepository;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallLifecycleService;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for Dialer Call lifecycle management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DialerCallLifecycleServiceImpl
        implements DialerCallLifecycleService {

    private final DialerCallRepository
            dialerCallRepository;

    private final CampaignContactService
            campaignContactService;

    @Override
    public void updateStatus(
            DialerCall call,
            CallAttemptStatus status) {

        if (call == null || status == null) {
            return;
        }

        call.setStatus(status);

        updateTimestamps(
                call,
                status
        );

        DialerCall savedCall =
                dialerCallRepository.save(
                        call
                );

        synchronizeCampaignContact(
                savedCall,
                status
        );
    }

    @Override
    public void markAnswered(
            DialerCall call) {

        if (call == null) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        call.setStatus(
                CallAttemptStatus.ANSWERED
        );

        if (call.getStartedAt() == null) {
            call.setStartedAt(now);
        }

        call.setAnsweredAt(now);

        DialerCall savedCall =
                dialerCallRepository.save(
                        call
                );

        synchronizeCampaignContact(
                savedCall,
                CallAttemptStatus.ANSWERED
        );
    }

    @Override
    public void completeCall(
            DialerCall call,
            Integer durationSeconds,
            String hangupReason) {

        if (call == null) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        call.setStatus(
                CallAttemptStatus.COMPLETED
        );

        call.setEndedAt(now);
        call.setDurationSeconds(
                durationSeconds
        );
        call.setHangupReason(
                hangupReason
        );

        DialerCall savedCall =
                dialerCallRepository.save(
                        call
                );

        synchronizeCampaignContact(
                savedCall,
                CallAttemptStatus.COMPLETED
        );
    }

    @Override
    public void failCall(
            DialerCall call,
            String failureReason) {

        if (call == null) {
            return;
        }

        call.setStatus(
                CallAttemptStatus.FAILED
        );

        call.setFailureReason(
                failureReason
        );

        call.setEndedAt(
                LocalDateTime.now()
        );

        DialerCall savedCall =
                dialerCallRepository.save(
                        call
                );

        synchronizeCampaignContact(
                savedCall,
                CallAttemptStatus.FAILED
        );
    }

    private void updateTimestamps(
            DialerCall call,
            CallAttemptStatus status) {

        LocalDateTime now =
                LocalDateTime.now();

        switch (status) {

            case DIALING:
            case RINGING:

                if (call.getStartedAt() == null) {
                    call.setStartedAt(now);
                }

                break;

            case ANSWERED:
            case IN_PROGRESS:

                if (call.getStartedAt() == null) {
                    call.setStartedAt(now);
                }

                if (status ==
                        CallAttemptStatus.ANSWERED
                        && call.getAnsweredAt() == null) {

                    call.setAnsweredAt(now);
                }

                break;

            case COMPLETED:
            case FAILED:
            case NO_ANSWER:
            case BUSY:
            case CANCELLED:
            case REJECTED:

                if (call.getEndedAt() == null) {
                    call.setEndedAt(now);
                }

                break;

            default:
                break;
        }
    }

    private void synchronizeCampaignContact(
            DialerCall call,
            CallAttemptStatus status) {

        if (call.getCampaignContact() == null) {
            return;
        }

        String campaignContactPublicId =
                call.getCampaignContact()
                        .getPublicId();

        if (campaignContactPublicId == null
                || campaignContactPublicId.isBlank()) {
            return;
        }

        switch (status) {

            case DIALING:
            case RINGING:
            case ANSWERED:
            case IN_PROGRESS:

                campaignContactService
                        .updateDialingStatus(
                                campaignContactPublicId,
                                CampaignContactConstants
                                        .STATUS_DIALING
                        );

                break;

            case NO_ANSWER:

                campaignContactService
                        .updateDialingStatus(
                                campaignContactPublicId,
                                CampaignContactConstants
                                        .STATUS_NO_ANSWER
                        );

                break;

            case BUSY:

                campaignContactService
                        .updateDialingStatus(
                                campaignContactPublicId,
                                CampaignContactConstants
                                        .STATUS_BUSY
                        );

                break;

            case FAILED:
            case CANCELLED:
            case REJECTED:

                campaignContactService
                        .updateDialingStatus(
                                campaignContactPublicId,
                                CampaignContactConstants
                                        .STATUS_FAILED
                        );

                break;

            case COMPLETED:

                /*
                 * Do not change the Campaign Contact back
                 * to PENDING. A successfully completed
                 * contact must not become eligible for a
                 * new dial automatically.
                 */
                log.debug(
                        "Campaign Contact call completed. "
                                + "Contact : {}",
                        campaignContactPublicId
                );

                break;

            default:
                break;
        }
    }
}