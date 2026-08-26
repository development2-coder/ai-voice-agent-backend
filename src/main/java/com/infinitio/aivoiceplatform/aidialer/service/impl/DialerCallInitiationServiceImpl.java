package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.constant.DialerMessages;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.repository.DialerCallRepository;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallInitiationService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallRecordService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallService;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactService;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for initiating queued Dialer Calls
 * through the configured telephony provider.
 *
 * <p>
 * This service is responsible only for initiating the
 * outbound provider call.
 * </p>
 *
 * <p>
 * Campaign Contact attempt count is incremented only after
 * the telephony provider successfully accepts the outbound
 * call and returns a provider call ID.
 * </p>
 *
 * <p>
 * CallSession creation and Flow execution are intentionally
 * NOT performed here. They are started only after the
 * telephony provider sends a CALL_ANSWERED event.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialerCallInitiationServiceImpl
        implements DialerCallInitiationService {

    private static final Integer NOT_DELETED = 0;

    private static final String EXOTEL_PROVIDER =
            "EXOTEL";

    private final DialerCallRepository
            dialerCallRepository;

    private final DialerCallService
            dialerCallService;

    private final DialerCallRecordService
            dialerCallRecordService;

    private final CampaignContactService
            campaignContactService;

    private final TelephonyService
            telephonyService;

    /**
     * Initiates a queued Dialer Call.
     *
     * <p>
     * Runtime sequence:
     *
     * <pre>
     * QUEUED
     *   ↓
     * DIALING
     *   ↓
     * Create platform Call
     *   ↓
     * Exotel outbound API
     *   ↓
     * Provider call ID received
     *   ↓
     * Campaign Contact attemptCount++
     *   ↓
     * Wait for webhook
     * </pre>
     *
     * <p>
     * CallSession is NOT created here.
     * Flow execution is NOT started here.
     * </p>
     *
     * @param dialerCallPublicId DialerCall public identifier
     * @return DialerCall response
     */
    @Override
    @Transactional
    public DialerCallResponse initiateCall(
            String dialerCallPublicId) {

        log.info(
                "Starting Dialer Call initiation. "
                        + "dialerCallPublicId={}",
                dialerCallPublicId
        );

        validatePublicId(
                dialerCallPublicId
        );

        DialerCall call =
                dialerCallRepository
                        .findByPublicIdAndIsDeleted(
                                dialerCallPublicId,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        DialerMessages
                                                .DIALER_CALL_NOT_FOUND
                                )
                        );

        log.info(
                "Dialer Call found. "
                        + "dialerCallPublicId={}, status={}, "
                        + "attemptNumber={}",
                call.getPublicId(),
                call.getStatus(),
                call.getAttemptNumber()
        );

        /*
         * ---------------------------------------------------------
         * STEP 1: Validate Dialer configuration.
         * ---------------------------------------------------------
         */
        validateDialerConfiguration(
                call
        );

        /*
         * ---------------------------------------------------------
         * STEP 2: Validate current status.
         * ---------------------------------------------------------
         */
        if (call.getStatus()
                != CallAttemptStatus.QUEUED) {

            log.warn(
                    "Dialer Call cannot be initiated because "
                            + "status is not QUEUED. "
                            + "dialerCallPublicId={}, status={}",
                    call.getPublicId(),
                    call.getStatus()
            );

            throw new IllegalStateException(
                    DialerMessages.INITIATION_ONLY_QUEUED
                            + " Current status: "
                            + call.getStatus()
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 3: Validate destination number.
         * ---------------------------------------------------------
         */
        if (call.getPhoneNumber() == null
                || call.getPhoneNumber().isBlank()) {

            markInitiationFailed(
                    call,
                    DialerMessages.PHONE_NUMBER_REQUIRED
            );

            throw new IllegalStateException(
                    DialerMessages.PHONE_NUMBER_REQUIRED
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 4: Validate Campaign Contact.
         * ---------------------------------------------------------
         */
        if (call.getCampaignContact() == null) {

            markInitiationFailed(
                    call,
                    DialerMessages
                            .CONTACT_REQUIRED_FOR_INITIATION
            );

            throw new IllegalStateException(
                    DialerMessages
                            .CONTACT_REQUIRED_FOR_INITIATION
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 5: Resolve campaign caller number.
         * ---------------------------------------------------------
         */
        String fromNumber =
                call.getDialer()
                        .getCampaign()
                        .getPhoneNumber()
                        .getPhoneNumber();

        if (fromNumber == null
                || fromNumber.isBlank()) {

            markInitiationFailed(
                    call,
                    DialerMessages
                            .CAMPAIGN_PHONE_NUMBER_NOT_CONFIGURED
            );

            throw new IllegalStateException(
                    DialerMessages
                            .CAMPAIGN_PHONE_NUMBER_NOT_CONFIGURED
            );
        }

        log.info(
                "Resolved campaign caller number. "
                        + "dialerCallPublicId={}, fromNumber={}",
                call.getPublicId(),
                fromNumber
        );

        /*
         * ---------------------------------------------------------
         * STEP 6: Mark DialerCall as DIALING.
         * ---------------------------------------------------------
         *
         * IMPORTANT:
         *
         * We do NOT increment CampaignContact.attemptCount
         * here.
         *
         * The actual attempt count is incremented only after
         * the provider accepts the outbound call.
         */
        call.setStatus(
                CallAttemptStatus.DIALING
        );

        if (call.getStartedAt() == null) {

            call.setStartedAt(
                    LocalDateTime.now()
            );
        }

        DialerCall dialingCall =
                dialerCallRepository.save(
                        call
                );

        log.info(
                "Dialer Call marked as DIALING. "
                        + "dialerCallPublicId={}, "
                        + "campaignContactPublicId={}",
                dialingCall.getPublicId(),
                dialingCall
                        .getCampaignContact()
                        .getPublicId()
        );

        try {

            /*
             * -----------------------------------------------------
             * STEP 7: Create platform Call.
             * -----------------------------------------------------
             */
            String callPublicId =
                    dialerCallRecordService
                            .createCallRecord(
                                    dialingCall,
                                    fromNumber
                            );

            log.info(
                    "Platform Call created. "
                            + "callPublicId={}, "
                            + "dialerCallPublicId={}",
                    callPublicId,
                    dialingCall.getPublicId()
            );

            /*
             * -----------------------------------------------------
             * STEP 8: Build provider request.
             * -----------------------------------------------------
             */
            PlaceOutboundCallRequestDto request =
                    PlaceOutboundCallRequestDto.builder()
                            .callPublicId(
                                    callPublicId
                            )
                            .fromNumber(
                                    fromNumber
                            )
                            .toNumber(
                                    dialingCall
                                            .getPhoneNumber()
                            )
                            .callbackUrl(
                                    null
                            )
                            .build();

            /*
             * -----------------------------------------------------
             * STEP 9: Place outbound Exotel call.
             * -----------------------------------------------------
             */
            log.info(
                    "Placing outbound Exotel call. "
                            + "callPublicId={}, "
                            + "dialerCallPublicId={}, "
                            + "destination={}",
                    callPublicId,
                    dialingCall.getPublicId(),
                    dialingCall.getPhoneNumber()
            );

            ProviderCallResponseDto providerResponse =
                    telephonyService
                            .placeOutboundCall(
                                    EXOTEL_PROVIDER,
                                    request
                            );

            /*
             * -----------------------------------------------------
             * STEP 10: Validate provider response.
             * -----------------------------------------------------
             */
            if (providerResponse == null) {

                log.error(
                        "Exotel returned null provider response. "
                                + "callPublicId={}, "
                                + "dialerCallPublicId={}",
                        callPublicId,
                        dialingCall.getPublicId()
                );

                markInitiationFailed(
                        dialingCall,
                        DialerMessages
                                .INITIATION_FAILED
                );

                throw new IllegalStateException(
                        DialerMessages
                                .INITIATION_FAILED
                );
            }

            String providerCallId =
                    providerResponse
                            .getProviderCallId();

            /*
             * -----------------------------------------------------
             * STEP 11: Provider call ID is mandatory.
             * -----------------------------------------------------
             *
             * Without providerCallId we cannot reliably map
             * the Exotel webhook back to this DialerCall.
             */
            if (providerCallId == null
                    || providerCallId.isBlank()) {

                log.error(
                        "Exotel response did not contain "
                                + "provider call ID. "
                                + "callPublicId={}, "
                                + "dialerCallPublicId={}",
                        callPublicId,
                        dialingCall.getPublicId()
                );

                markInitiationFailed(
                        dialingCall,
                        DialerMessages
                                .INITIATION_FAILED
                );

                throw new IllegalStateException(
                        DialerMessages
                                .INITIATION_FAILED
                );
            }

            /*
             * -----------------------------------------------------
             * STEP 12: Store provider call ID.
             * -----------------------------------------------------
             */
            dialingCall.setExotelCallId(
                    providerCallId
            );

            log.info(
                    "Exotel provider call ID stored. "
                            + "callPublicId={}, "
                            + "dialerCallPublicId={}, "
                            + "providerCallId={}",
                    callPublicId,
                    dialingCall.getPublicId(),
                    providerCallId
            );

            /*
             * -----------------------------------------------------
             * STEP 13: INCREMENT CAMPAIGN CONTACT ATTEMPT.
             * -----------------------------------------------------
             *
             * This is the important fix.
             *
             * One DialerCall corresponds to one actual outbound
             * provider attempt.
             *
             * Therefore:
             *
             * CampaignContact.attemptCount++
             * CampaignContact.lastAttemptAt = now
             * CampaignContact.status = DIALING
             */
            String campaignContactPublicId =
                    dialingCall
                            .getCampaignContact()
                            .getPublicId();

            log.info(
                    "Marking Campaign Contact as DIALING "
                            + "and incrementing attempt count. "
                            + "campaignContactPublicId={}, "
                            + "dialerCallPublicId={}, "
                            + "attemptNumber={}",
                    campaignContactPublicId,
                    dialingCall.getPublicId(),
                    dialingCall.getAttemptNumber()
            );

            campaignContactService.markDialing(
                    campaignContactPublicId
            );

            log.info(
                    "Campaign Contact attempt count updated. "
                            + "campaignContactPublicId={}, "
                            + "dialerCallPublicId={}",
                    campaignContactPublicId,
                    dialingCall.getPublicId()
            );

            /*
             * -----------------------------------------------------
             * STEP 14: Synchronize immediate provider status.
             * -----------------------------------------------------
             *
             * The webhook remains authoritative for the actual
             * call lifecycle.
             */
            if (providerResponse.getStatus() != null
                    && !providerResponse
                    .getStatus()
                    .isBlank()) {

                CallAttemptStatus providerStatus =
                        mapProviderStatus(
                                providerResponse
                                        .getStatus()
                        );

                dialingCall.setStatus(
                        providerStatus
                );

                log.info(
                        "Immediate Exotel status mapped. "
                                + "providerStatus={}, "
                                + "dialerStatus={}, "
                                + "dialerCallPublicId={}",
                        providerResponse.getStatus(),
                        providerStatus,
                        dialingCall.getPublicId()
                );
            }

            /*
             * -----------------------------------------------------
             * STEP 15: Persist DialerCall.
             * -----------------------------------------------------
             */
            DialerCall savedCall =
                    dialerCallRepository.save(
                            dialingCall
                    );

            /*
             * -----------------------------------------------------
             * IMPORTANT:
             *
             * DO NOT CREATE CALL SESSION HERE.
             *
             * DO NOT START FLOW HERE.
             *
             * Wait for CALL_ANSWERED webhook.
             * -----------------------------------------------------
             */
            log.info(
                    "Outbound Exotel call submitted successfully. "
                            + "Waiting for CALL_ANSWERED webhook. "
                            + "dialerCallPublicId={}, "
                            + "callPublicId={}, "
                            + "providerCallId={}, "
                            + "status={}, "
                            + "attemptNumber={}",
                    savedCall.getPublicId(),
                    callPublicId,
                    savedCall.getExotelCallId(),
                    savedCall.getStatus(),
                    savedCall.getAttemptNumber()
            );

            return dialerCallService.getByPublicId(
                    savedCall.getPublicId()
            );

        } catch (Exception exception) {

            log.error(
                    "Dialer Call initiation failed. "
                            + "dialerCallPublicId={}, "
                            + "attemptNumber={}, "
                            + "reason={}",
                    dialingCall.getPublicId(),
                    dialingCall.getAttemptNumber(),
                    exception.getMessage(),
                    exception
            );

            markInitiationFailed(
                    dialingCall,
                    exception.getMessage()
            );

            throw exception;
        }
    }

    /**
     * Validates required Dialer configuration.
     *
     * @param call DialerCall
     */
    private void validateDialerConfiguration(
            DialerCall call) {

        if (call == null) {

            throw new IllegalArgumentException(
                    DialerMessages
                            .DIALER_CALL_NOT_FOUND
            );
        }

        if (call.getDialer() == null) {

            markInitiationFailed(
                    call,
                    DialerMessages
                            .DIALER_REQUIRED_FOR_INITIATION
            );

            throw new IllegalStateException(
                    DialerMessages
                            .DIALER_REQUIRED_FOR_INITIATION
            );
        }

        if (call.getDialer()
                .getCampaign() == null) {

            markInitiationFailed(
                    call,
                    DialerMessages
                            .DIALER_CAMPAIGN_NOT_CONFIGURED
            );

            throw new IllegalStateException(
                    DialerMessages
                            .DIALER_CAMPAIGN_NOT_CONFIGURED
            );
        }

        if (call.getDialer()
                .getCampaign()
                .getPhoneNumber() == null) {

            markInitiationFailed(
                    call,
                    DialerMessages
                            .CAMPAIGN_PHONE_NUMBER_NOT_CONFIGURED
            );

            throw new IllegalStateException(
                    DialerMessages
                            .CAMPAIGN_PHONE_NUMBER_NOT_CONFIGURED
            );
        }
    }

    /**
     * Maps the immediate Exotel status to DialerCall status.
     *
     * @param providerStatus provider status
     * @return internal DialerCall status
     */
    private CallAttemptStatus mapProviderStatus(
            String providerStatus) {

        if (providerStatus == null
                || providerStatus.isBlank()) {

            return CallAttemptStatus.DIALING;
        }

        return switch (
                providerStatus
                        .trim()
                        .toLowerCase(
                                java.util.Locale.ROOT
                        )
                ) {

            case "initiated",
                 "queued",
                 "in-progress" ->

                    CallAttemptStatus.DIALING;

            case "ringing" ->

                    CallAttemptStatus.RINGING;

            case "answered" ->

                    CallAttemptStatus.ANSWERED;

            case "completed" ->

                    CallAttemptStatus.COMPLETED;

            case "failed" ->

                    CallAttemptStatus.FAILED;

            case "busy" ->

                    CallAttemptStatus.BUSY;

            case "no-answer",
                 "no_answer" ->

                    CallAttemptStatus.NO_ANSWER;

            case "canceled",
                 "cancelled" ->

                    CallAttemptStatus.CANCELLED;

            case "rejected" ->

                    CallAttemptStatus.REJECTED;

            default -> {

                log.warn(
                        "Unknown Exotel provider status "
                                + "during call initiation. "
                                + "providerStatus={}",
                        providerStatus
                );

                yield CallAttemptStatus.DIALING;
            }
        };
    }

    /**
     * Marks DialerCall initiation as failed.
     *
     * @param call DialerCall
     * @param reason failure reason
     */
    private void markInitiationFailed(
            DialerCall call,
            String reason) {

        if (call == null) {
            return;
        }

        call.setStatus(
                CallAttemptStatus.FAILED
        );

        call.setFailureReason(
                reason
        );

        call.setEndedAt(
                LocalDateTime.now()
        );

        dialerCallRepository.save(
                call
        );

        log.error(
                "Dialer Call marked as FAILED. "
                        + "dialerCallPublicId={}, reason={}",
                call.getPublicId(),
                reason
        );
    }

    /**
     * Validates DialerCall public ID.
     *
     * @param publicId DialerCall public ID
     */
    private void validatePublicId(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new IllegalArgumentException(
                    DialerMessages
                            .DIALER_CALL_PUBLIC_ID_REQUIRED
            );
        }
    }
}