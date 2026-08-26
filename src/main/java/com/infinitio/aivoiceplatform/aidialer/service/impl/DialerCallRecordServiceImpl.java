package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerMessages;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallRecordService;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the master Call record for AI Dialer attempts.
 *
 * <p>
 * The Call entity is the platform-level call record,
 * while DialerCall represents the individual dialer attempt.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialerCallRecordServiceImpl
        implements DialerCallRecordService {

    private static final String PROVIDER_EXOTEL =
            "EXOTEL";

    private static final String DIRECTION_OUTBOUND =
            "OUTBOUND";

    private static final String STATUS_INITIATED =
            "INITIATED";

    private final CallRepository callRepository;

    /**
     * Creates a master Call record for a DialerCall.
     *
     * @param dialerCall dialer call attempt
     * @param fromNumber caller number
     * @return Call public identifier
     */
    @Override
    @Transactional
    public String createCallRecord(
            DialerCall dialerCall,
            String fromNumber) {

        if (dialerCall == null) {

            throw new BadRequestException(
                    DialerMessages.INITIATION_FAILED
            );
        }

        if (dialerCall.getCampaignContact() == null) {

            throw new BadRequestException(
                    DialerMessages
                            .CONTACT_REQUIRED_FOR_INITIATION
            );
        }

        if (fromNumber == null
                || fromNumber.isBlank()) {

            throw new BadRequestException(
                    DialerMessages.PHONE_NUMBER_REQUIRED
            );
        }

        String toNumber =
                dialerCall.getPhoneNumber();

        if (toNumber == null
                || toNumber.isBlank()) {

            throw new BadRequestException(
                    DialerMessages.PHONE_NUMBER_REQUIRED
            );
        }

        /*
         * Create the platform-level Call record.
         *
         * Provider Call ID is intentionally left null.
         * Exotel returns it only after the provider request.
         */
        Call call =
                Call.builder()
                        .campaignContact(
                                dialerCall
                                        .getCampaignContact()
                        )
                        .provider(
                                PROVIDER_EXOTEL
                        )
                        .fromNumber(
                                fromNumber
                        )
                        .toNumber(
                                toNumber
                        )
                        .direction(
                                DIRECTION_OUTBOUND
                        )
                        .status(
                                STATUS_INITIATED
                        )
                        .createdBy(
                                resolveCreatedBy(
                                        dialerCall
                                )
                        )
                        .build();

        Call savedCall =
                callRepository.save(
                        call
                );

        log.info(
                "Master Call created for DialerCall. "
                        + "callPublicId={}, dialerCallPublicId={}, "
                        + "campaignContactPublicId={}",
                savedCall.getPublicId(),
                dialerCall.getPublicId(),
                dialerCall
                        .getCampaignContact()
                        .getPublicId()
        );

        return savedCall.getPublicId();
    }

    /**
     * Resolves the audit user from the Dialer.
     *
     * <p>
     * AI Dialer calls can be initiated by the scheduler,
     * where there is no authenticated HTTP user context.
     * Therefore the Dialer's creator is used for the
     * Call audit record.
     * </p>
     *
     * @param dialerCall dialer call
     * @return creator user ID
     */
    private Long resolveCreatedBy(
            DialerCall dialerCall) {

        if (dialerCall.getDialer() == null
                || dialerCall.getDialer().getCreatedBy() == null) {

            throw new BadRequestException(
                    DialerMessages
                            .DIALER_REQUIRED_FOR_INITIATION
            );
        }

        return dialerCall
                .getDialer()
                .getCreatedBy();
    }
}