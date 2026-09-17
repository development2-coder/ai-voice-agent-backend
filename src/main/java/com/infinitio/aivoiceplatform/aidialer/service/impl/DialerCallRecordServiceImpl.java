package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerMessages;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallRecordService;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionCreateService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Creates the master Call and CallSession records
 * required for AI Dialer outbound calls.
 *
 * <p>
 * Both Call and CallSession are created before the
 * telephony provider is contacted. This ensures that
 * the provider WebSocket can immediately resolve the
 * application runtime context when the call is answered.
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

    private final CallSessionCreateService
            callSessionCreateService;

    /**
     * Creates the master Call and CallSession records
     * before the provider outbound request.
     *
     * <p>
     * A separate transaction is intentionally used so that
     * the Call and CallSession are committed before Exotel
     * attempts to establish the WebSocket media stream.
     * </p>
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

        validateRequest(
                dialerCall,
                fromNumber
        );

        String toNumber =
                dialerCall.getPhoneNumber();

        /*
         * ---------------------------------------------------------
         * STEP 1: CREATE MASTER CALL
         * ---------------------------------------------------------
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
                        .startedAt(
                                LocalDateTime.now()
                        )
                        .createdBy(
                                resolveCreatedBy(
                                        dialerCall
                                )
                        )
                        .build();

        Call savedCall =
                callRepository.saveAndFlush(
                        call
                );

        log.info(
                "Master Call created for AI Dialer. "
                        + "callPublicId={}, "
                        + "dialerCallPublicId={}, "
                        + "campaignContactPublicId={}",
                savedCall.getPublicId(),
                dialerCall.getPublicId(),
                dialerCall
                        .getCampaignContact()
                        .getPublicId()
        );

        /*
         * ---------------------------------------------------------
         * STEP 2: VALIDATE RUNTIME CONFIGURATION
         * ---------------------------------------------------------
         */
        validateRuntimeConfiguration(
                dialerCall
        );

        /*
         * ---------------------------------------------------------
         * STEP 3: CREATE CALL SESSION
         * ---------------------------------------------------------
         *
         * IMPORTANT:
         *
         * The CallSession is created before Exotel is contacted.
         * The Exotel WebSocket START event can therefore resolve
         * the runtime context immediately.
         */
        CreateCallSessionRequestDto
                callSessionRequest =
                CreateCallSessionRequestDto
                        .builder()
                        .callId(
                                savedCall.getPublicId()
                        )
                        .tenantId(
                                dialerCall
                                        .getDialer()
                                        .getAgent()
                                        .getTenant()
                                        .getPublicId()
                        )
                        .agentId(
                                dialerCall
                                        .getDialer()
                                        .getAgent()
                                        .getPublicId()
                        )
                        .agentVersion(
                                dialerCall
                                        .getDialer()
                                        .getFlow()
                                        .getVersion()
                        )
                        .flowPublicId(
                                dialerCall
                                        .getDialer()
                                        .getFlow()
                                        .getPublicId()
                        )
                        .language(
                                dialerCall
                                        .getDialer()
                                        .getAgent()
                                        .getLanguage()
                        )
                        .build();

        callSessionCreateService
                .createCallSession(
                        callSessionRequest,
                        resolveCreatedBy(
                                dialerCall
                        )
                );

        log.info(
                "CallSession created before provider call. "
                        + "callPublicId={}, "
                        + "agentPublicId={}, "
                        + "flowPublicId={}, "
                        + "flowVersion={}",
                savedCall.getPublicId(),
                callSessionRequest.getAgentId(),
                callSessionRequest.getFlowPublicId(),
                callSessionRequest.getAgentVersion()
        );

        /*
         * The REQUIRES_NEW transaction commits here before
         * this method returns.
         */
        return savedCall.getPublicId();
    }

    /**
     * Validates the input required to create Call
     * and CallSession records.
     *
     * @param dialerCall dialer call
     * @param fromNumber caller number
     */
    private void validateRequest(
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

        if (dialerCall.getPhoneNumber() == null
                || dialerCall.getPhoneNumber().isBlank()) {

            throw new BadRequestException(
                    DialerMessages.PHONE_NUMBER_REQUIRED
            );
        }
    }

    /**
     * Validates Agent, Flow and Tenant configuration
     * required by the runtime CallSession.
     *
     * @param dialerCall dialer call
     */
    private void validateRuntimeConfiguration(
            DialerCall dialerCall) {

        if (dialerCall.getDialer() == null) {

            throw new BadRequestException(
                    DialerMessages
                            .DIALER_REQUIRED_FOR_INITIATION
            );
        }

        if (dialerCall.getDialer().getAgent() == null) {

            throw new IllegalStateException(
                    "Agent is not configured for AI Dialer."
            );
        }

        if (dialerCall.getDialer().getFlow() == null) {

            throw new IllegalStateException(
                    "Flow is not configured for AI Dialer."
            );
        }

        if (dialerCall
                .getDialer()
                .getAgent()
                .getTenant() == null) {

            throw new IllegalStateException(
                    "Tenant is not configured for AI Dialer Agent."
            );
        }

        if (dialerCall
                .getDialer()
                .getFlow()
                .getVersion() == null
                || dialerCall
                .getDialer()
                .getFlow()
                .getVersion() <= 0) {

            throw new IllegalStateException(
                    "Flow version is not configured."
            );
        }

        if (dialerCall
                .getDialer()
                .getAgent()
                .getPublicId() == null
                || dialerCall
                .getDialer()
                .getAgent()
                .getPublicId()
                .isBlank()) {

            throw new IllegalStateException(
                    "Agent public ID is not configured."
            );
        }

        if (dialerCall
                .getDialer()
                .getFlow()
                .getPublicId() == null
                || dialerCall
                .getDialer()
                .getFlow()
                .getPublicId()
                .isBlank()) {

            throw new IllegalStateException(
                    "Flow public ID is not configured."
            );
        }

        if (dialerCall
                .getDialer()
                .getAgent()
                .getTenant()
                .getPublicId() == null
                || dialerCall
                .getDialer()
                .getAgent()
                .getTenant()
                .getPublicId()
                .isBlank()) {

            throw new IllegalStateException(
                    "Tenant public ID is not configured."
            );
        }
    }

    /**
     * Resolves the audit user from the Dialer.
     *
     * <p>
     * AI Dialer calls can be initiated by the scheduler,
     * where there is no authenticated HTTP user context.
     * Therefore the Dialer's creator is used for the
     * Call and CallSession audit records.
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