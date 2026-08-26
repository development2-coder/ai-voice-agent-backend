package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.repository.DialerCallRepository;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallLifecycleService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallWebhookService;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionFlowRuntimeService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionRuntimeService;
import com.infinitio.aivoiceplatform.telephony.constant.TelephonyConstants;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Synchronizes telephony provider webhook events
 * with AI Dialer call attempts.
 *
 * <p>
 * The CallSession runtime is started only when the
 * customer has actually answered the call.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DialerCallWebhookServiceImpl
        implements DialerCallWebhookService {

    private final DialerCallRepository
            dialerCallRepository;

    private final DialerCallLifecycleService
            dialerCallLifecycleService;

    private final CallRepository
            callRepository;

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionRuntimeService
            callSessionRuntimeService;

    private final CallSessionFlowRuntimeService
            callSessionFlowRuntimeService;

    /**
     * Processes a normalized telephony provider event.
     *
     * @param event normalized provider event
     */
    @Override
    public void synchronize(
            NormalizedCallEventDto event) {

        if (event == null) {

            log.debug(
                    "Ignoring null telephony webhook event."
            );

            return;
        }

        String providerCallId =
                event.getProviderCallId();

        if (providerCallId == null
                || providerCallId.isBlank()) {

            log.warn(
                    "Ignoring telephony webhook without "
                            + "provider call ID. event={}",
                    event.getEvent()
            );

            return;
        }

        log.info(
                "Processing AI Dialer telephony webhook. "
                        + "providerCallId={}, event={}, provider={}",
                providerCallId,
                event.getEvent(),
                event.getProvider()
        );

        /*
         * ---------------------------------------------------------
         * STEP 1: Find DialerCall.
         * ---------------------------------------------------------
         */
        DialerCall dialerCall =
                dialerCallRepository
                        .findByExotelCallId(
                                providerCallId
                        )
                        .orElse(null);

        /*
         * Not every provider call belongs to AI Dialer.
         */
        if (dialerCall == null) {

            log.debug(
                    "No AI Dialer call found for providerCallId={}. "
                            + "Ignoring webhook.",
                    providerCallId
            );

            return;
        }

        String normalizedEvent =
                normalizeEvent(
                        event.getEvent()
                );

        if (normalizedEvent == null) {

            log.warn(
                    "Unsupported normalized telephony event. "
                            + "providerCallId={}, event={}",
                    providerCallId,
                    event.getEvent()
            );

            return;
        }

        log.info(
                "AI Dialer webhook resolved. "
                        + "dialerCallPublicId={}, "
                        + "providerCallId={}, "
                        + "event={}",
                dialerCall.getPublicId(),
                providerCallId,
                normalizedEvent
        );

        /*
         * ---------------------------------------------------------
         * CALL INITIATED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_INITIATED
                .equals(
                        normalizedEvent
                )) {

            dialerCallLifecycleService
                    .updateStatus(
                            dialerCall,
                            CallAttemptStatus.DIALING
                    );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL RINGING
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_RINGING
                .equals(
                        normalizedEvent
                )) {

            log.info(
                    "Customer phone is ringing. "
                            + "CallSession will not be created. "
                            + "dialerCallPublicId={}",
                    dialerCall.getPublicId()
            );

            dialerCallLifecycleService
                    .updateStatus(
                            dialerCall,
                            CallAttemptStatus.RINGING
                    );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL ANSWERED
         * ---------------------------------------------------------
         *
         * THIS is where CallSession creation starts.
         */
        if (TelephonyConstants.EVENT_CALL_ANSWERED
                .equals(
                        normalizedEvent
                )) {

            handleAnswered(
                    dialerCall,
                    providerCallId
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL COMPLETED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_COMPLETED
                .equals(
                        normalizedEvent
                )
                || TelephonyConstants.EVENT_CALL_ENDED
                .equals(
                        normalizedEvent
                )) {

            log.info(
                    "AI Dialer call completed. "
                            + "dialerCallPublicId={}, "
                            + "providerCallId={}",
                    dialerCall.getPublicId(),
                    providerCallId
            );

            dialerCallLifecycleService
                    .completeCall(
                            dialerCall,
                            null,
                            normalizedEvent
                    );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL FAILED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_FAILED
                .equals(
                        normalizedEvent
                )) {

            log.warn(
                    "AI Dialer call failed. "
                            + "dialerCallPublicId={}, "
                            + "providerCallId={}",
                    dialerCall.getPublicId(),
                    providerCallId
            );

            dialerCallLifecycleService
                    .updateStatus(
                            dialerCall,
                            CallAttemptStatus.FAILED
                    );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL NO ANSWER
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_NO_ANSWER
                .equals(
                        normalizedEvent
                )) {

            log.info(
                    "AI Dialer call was not answered. "
                            + "dialerCallPublicId={}, "
                            + "providerCallId={}",
                    dialerCall.getPublicId(),
                    providerCallId
            );

            dialerCallLifecycleService
                    .updateStatus(
                            dialerCall,
                            CallAttemptStatus.NO_ANSWER
                    );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL BUSY
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_BUSY
                .equals(
                        normalizedEvent
                )) {

            log.info(
                    "AI Dialer call destination was busy. "
                            + "dialerCallPublicId={}, "
                            + "providerCallId={}",
                    dialerCall.getPublicId(),
                    providerCallId
            );

            dialerCallLifecycleService
                    .updateStatus(
                            dialerCall,
                            CallAttemptStatus.BUSY
                    );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL CANCELLED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_CANCELLED
                .equals(
                        normalizedEvent
                )) {

            log.info(
                    "AI Dialer call was cancelled. "
                            + "dialerCallPublicId={}, "
                            + "providerCallId={}",
                    dialerCall.getPublicId(),
                    providerCallId
            );

            dialerCallLifecycleService
                    .updateStatus(
                            dialerCall,
                            CallAttemptStatus.CANCELLED
                    );

            return;
        }

        /*
         * ---------------------------------------------------------
         * CALL REJECTED
         * ---------------------------------------------------------
         */
        if (TelephonyConstants.EVENT_CALL_REJECTED
                .equals(
                        normalizedEvent
                )) {

            log.info(
                    "AI Dialer call was rejected. "
                            + "dialerCallPublicId={}, "
                            + "providerCallId={}",
                    dialerCall.getPublicId(),
                    providerCallId
            );

            dialerCallLifecycleService
                    .updateStatus(
                            dialerCall,
                            CallAttemptStatus.REJECTED
                    );
        }
    }

    /**
     * Handles an answered call.
     *
     * <p>
     * The existing platform Call is located using the
     * Exotel provider call ID. DialerCall intentionally
     * does not contain a Call relationship.
     * </p>
     *
     * @param dialerCall AI Dialer call
     * @param providerCallId provider call identifier
     */
    private void handleAnswered(
            DialerCall dialerCall,
            String providerCallId) {

        log.info(
                "Customer answered AI Dialer call. "
                        + "dialerCallPublicId={}, "
                        + "providerCallId={}",
                dialerCall.getPublicId(),
                providerCallId
        );

        /*
         * ---------------------------------------------------------
         * STEP 1: Update DialerCall.
         * ---------------------------------------------------------
         */
        dialerCallLifecycleService
                .markAnswered(
                        dialerCall
                );

        /*
         * ---------------------------------------------------------
         * STEP 2: Find platform Call.
         * ---------------------------------------------------------
         *
         * DialerCall.exotelCallId
         *              ↓
         * Call.providerCallId
         */
        Call call =
                callRepository
                        .findByProviderCallId(
                                providerCallId
                        )
                        .orElse(null);

        if (call == null) {

            log.error(
                    "Platform Call not found for answered "
                            + "Exotel call. "
                            + "providerCallId={}, "
                            + "dialerCallPublicId={}",
                    providerCallId,
                    dialerCall.getPublicId()
            );

            throw new IllegalStateException(
                    "Platform Call not found for provider call ID: "
                            + providerCallId
            );
        }

        String callPublicId =
                call.getPublicId();

        log.info(
                "Platform Call resolved from provider call ID. "
                        + "providerCallId={}, callPublicId={}, "
                        + "dialerCallPublicId={}",
                providerCallId,
                callPublicId,
                dialerCall.getPublicId()
        );

        /*
         * ---------------------------------------------------------
         * STEP 3: Validate Dialer runtime configuration.
         * ---------------------------------------------------------
         */
        if (dialerCall.getDialer() == null) {

            throw new IllegalStateException(
                    "AI Dialer configuration is missing."
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

        if (dialerCall.getDialer()
                .getAgent()
                .getTenant() == null) {

            throw new IllegalStateException(
                    "Tenant is not configured for AI Dialer Agent."
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 4: Resolve runtime values.
         * ---------------------------------------------------------
         */
        Integer runtimeVersion =
                dialerCall
                        .getDialer()
                        .getFlow()
                        .getVersion();

        if (runtimeVersion == null
                || runtimeVersion <= 0) {

            throw new IllegalStateException(
                    "Flow version is not configured."
            );
        }

        String tenantPublicId =
                dialerCall
                        .getDialer()
                        .getAgent()
                        .getTenant()
                        .getPublicId();

        String agentPublicId =
                dialerCall
                        .getDialer()
                        .getAgent()
                        .getPublicId();

        String flowPublicId =
                dialerCall
                        .getDialer()
                        .getFlow()
                        .getPublicId();

        String language =
                dialerCall
                        .getDialer()
                        .getAgent()
                        .getLanguage();

        Long createdBy =
                dialerCall
                        .getDialer()
                        .getCreatedBy();

        log.info(
                "AI Dialer runtime configuration resolved. "
                        + "callPublicId={}, tenantPublicId={}, "
                        + "agentPublicId={}, flowPublicId={}, "
                        + "flowVersion={}",
                callPublicId,
                tenantPublicId,
                agentPublicId,
                flowPublicId,
                runtimeVersion
        );

        /*
         * ---------------------------------------------------------
         * STEP 5: Check for an existing CallSession.
         * ---------------------------------------------------------
         *
         * Provider webhooks can be retried.
         *
         * Therefore we must not create multiple sessions
         * for the same Call.
         */
        boolean sessionExists =
                callSessionRepository
                        .existsByCallId(
                                callPublicId
                        );

        if (sessionExists) {

            log.info(
                    "CallSession already exists. "
                            + "Skipping duplicate session creation. "
                            + "callPublicId={}",
                    callPublicId
            );

        } else {

            /*
             * -----------------------------------------------------
             * STEP 6: Create CallSession.
             * -----------------------------------------------------
             */
            CallSessionResponseDto session =
                    callSessionRuntimeService
                            .startSession(
                                    callPublicId,
                                    tenantPublicId,
                                    agentPublicId,
                                    runtimeVersion,
                                    flowPublicId,
                                    language,
                                    createdBy
                            );

            log.info(
                    "CallSession created after customer answered. "
                            + "callPublicId={}, sessionCallId={}",
                    callPublicId,
                    session != null
                            ? session.getCallId()
                            : null
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 7: Start Flow.
         * ---------------------------------------------------------
         *
         * startFlow() already contains duplicate execution
         * protection through flowExecutionPublicId.
         */
        CallSessionResponseDto flowSession =
                callSessionFlowRuntimeService
                        .startFlow(
                                callPublicId,
                                flowPublicId,
                                language,
                                null
                        );

        log.info(
                "AI Flow runtime started after customer answered. "
                        + "callPublicId={}, execution={}, node={}",
                callPublicId,
                flowSession != null
                        ? flowSession
                        .getFlowExecutionPublicId()
                        : null,
                flowSession != null
                        ? flowSession
                        .getFlowNodeId()
                        : null
        );
    }

    /**
     * Normalizes normalized event values.
     *
     * <p>
     * TelephonyConstants currently contain values such as
     * {@code call.answered}, not {@code CALL_ANSWERED}.
     * Therefore the comparison must use the actual constant
     * values instead of converting the value to enum-style text.
     * </p>
     *
     * @param event normalized event
     * @return normalized event constant value
     */
    private String normalizeEvent(
            String event) {

        if (event == null
                || event.isBlank()) {

            return null;
        }

        String normalized =
                event.trim()
                        .toLowerCase(
                                java.util.Locale.ROOT
                        );

        return switch (normalized) {

            case "call.initiated",
                 "call.started" ->

                    TelephonyConstants
                            .EVENT_CALL_INITIATED;

            case "call.ringing" ->

                    TelephonyConstants
                            .EVENT_CALL_RINGING;

            case "call.answered",
                 "call.in-progress" ->

                    TelephonyConstants
                            .EVENT_CALL_ANSWERED;

            case "call.completed" ->

                    TelephonyConstants
                            .EVENT_CALL_COMPLETED;

            case "call.ended" ->

                    TelephonyConstants
                            .EVENT_CALL_ENDED;

            case "call.failed" ->

                    TelephonyConstants
                            .EVENT_CALL_FAILED;

            case "call.no-answer",
                 "call.no_answer" ->

                    TelephonyConstants
                            .EVENT_CALL_NO_ANSWER;

            case "call.busy" ->

                    TelephonyConstants
                            .EVENT_CALL_BUSY;

            case "call.cancelled",
                 "call.canceled" ->

                    TelephonyConstants
                            .EVENT_CALL_CANCELLED;

            case "call.rejected" ->

                    TelephonyConstants
                            .EVENT_CALL_REJECTED;

            default -> {

                log.warn(
                        "Unknown telephony event received. "
                                + "event={}",
                        event
                );

                yield null;
            }
        };
    }
}