package com.infinitio.aivoiceplatform.telephony.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.telephony.dto.request.HangupCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.ProvisionNumberRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NumberResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.provider.TelephonyProvider;
import com.infinitio.aivoiceplatform.telephony.provider.TelephonyProviderRegistry;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyCallEventService;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyCallStateService;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Provider-independent telephony orchestration service.
 *
 * <p>
 * Provider-specific behavior remains inside the
 * TelephonyProvider implementation.
 * </p>
 *
 * <p>
 * Call state management, event persistence and AI Dialer
 * synchronization are delegated to dedicated services.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelephonyServiceImpl
        implements TelephonyService {

    private static final Integer NOT_DELETED = 0;

    private static final String CALL_NOT_FOUND =
            "Call not found.";

    private final TelephonyProviderRegistry
            telephonyProviderRegistry;

    private final CallRepository
            callRepository;

    private final TelephonyCallStateService
            telephonyCallStateService;

    private final TelephonyCallEventService
            telephonyCallEventService;

    private final DialerCallWebhookService
            dialerCallWebhookService;

    @Override
    public NumberResponseDto provisionNumber(
            String providerCode,
            ProvisionNumberRequestDto request) {

        log.info(
                "Provisioning telephony number. provider={}",
                providerCode
        );

        TelephonyProvider provider =
                getProvider(
                        providerCode
                );

        return provider.provisionNumber(
                request
        );
    }

    /**
     * Places an outbound call.
     *
     * <p>
     * The Call record is initialized and persisted before
     * the external provider call. Provider-specific response
     * information is persisted afterwards.
     * </p>
     */
    @Override
    public ProviderCallResponseDto placeOutboundCall(
            String providerCode,
            PlaceOutboundCallRequestDto request) {

        validateOutboundRequest(
                request
        );

        log.info(
                "Placing outbound call. "
                        + "provider={}, callPublicId={}",
                providerCode,
                request.getCallPublicId()
        );

        Call call =
                getCall(
                        request.getCallPublicId()
                );

        TelephonyProvider provider =
                getProvider(
                        providerCode
                );

        telephonyCallStateService
                .initializeOutboundCall(
                        call,
                        providerCode,
                        request.getFromNumber(),
                        request.getToNumber()
                );

        callRepository.save(
                call
        );

        try {

            ProviderCallResponseDto response =
                    provider.placeOutboundCall(
                            request
                    );

            if (response == null) {

                telephonyCallStateService
                        .markFailed(
                                call,
                                "Provider returned an empty response."
                        );

                callRepository.save(
                        call
                );

                throw new IllegalStateException(
                        "Provider returned an empty response."
                );
            }

            telephonyCallStateService
                    .updateFromProviderResponse(
                            call,
                            response
                    );

            callRepository.save(
                    call
            );

            log.info(
                    "Outbound call persisted successfully. "
                            + "callPublicId={}, providerCallId={}",
                    call.getPublicId(),
                    call.getProviderCallId()
            );

            return response;

        } catch (Exception exception) {

            log.error(
                    "Outbound call failed. "
                            + "callPublicId={}, provider={}",
                    request.getCallPublicId(),
                    providerCode,
                    exception
            );

            telephonyCallStateService
                    .markFailed(
                            call,
                            exception.getMessage()
                    );

            callRepository.save(
                    call
            );

            throw exception;
        }
    }

    /**
     * Processes a provider webhook.
     */
    @Override
    public NormalizedCallEventDto processInboundCall(
            String providerCode,
            String webhookPayload) {

        log.info(
                "Processing telephony webhook. provider={}",
                providerCode
        );

        TelephonyProvider provider =
                getProvider(
                        providerCode
                );

        NormalizedCallEventDto event =
                provider.normalizeCallEvent(
                        webhookPayload
                );

        if (event == null) {

            log.warn(
                    "Provider returned an empty normalized event. "
                            + "provider={}",
                    providerCode
            );

            return null;
        }

        if (event.getProviderCallId() == null
                || event
                .getProviderCallId()
                .isBlank()) {

            log.warn(
                    "Webhook does not contain a provider call ID. "
                            + "provider={}",
                    providerCode
            );

            return event;
        }

        Call call =
                callRepository
                        .findByProviderCallId(
                                event.getProviderCallId()
                        )
                        .orElse(null);

        if (call == null) {

            log.warn(
                    "Call not found for providerCallId={}",
                    event.getProviderCallId()
            );

            return event;
        }

        /*
         * 1. Update master Call.
         */
        telephonyCallStateService
                .updateFromWebhookEvent(
                        call,
                        event
                );

        callRepository.save(
                call
        );

        /*
         * 2. Persist normalized provider event.
         */
        telephonyCallEventService.save(
                call,
                event
        );

        /*
         * 3. Synchronize AI Dialer when this Call belongs
         * to a DialerCall.
         *
         * This is intentionally after the Call has been
         * updated and the event has been normalized.
         */
        dialerCallWebhookService.synchronize(
                event
        );

        log.info(
                "Telephony call processed successfully. "
                        + "callPublicId={}, providerCallId={}, event={}",
                call.getPublicId(),
                call.getProviderCallId(),
                event.getEvent()
        );

        return event;
    }

    @Override
    public void transferCall(
            String providerCode,
            TransferCallRequestDto request) {

        log.info(
                "Transferring call. provider={}",
                providerCode
        );

        TelephonyProvider provider =
                getProvider(
                        providerCode
                );

        provider.transferCall(
                request
        );
    }

    @Override
    public void hangupCall(
            String providerCode,
            HangupCallRequestDto request) {

        log.info(
                "Hanging up call. provider={}",
                providerCode
        );

        TelephonyProvider provider =
                getProvider(
                        providerCode
                );

        provider.hangupCall(
                request
        );
    }

    /**
     * Resolves a telephony provider from the registry.
     */
    private TelephonyProvider getProvider(
            String providerCode) {

        return telephonyProviderRegistry
                .getProvider(
                        providerCode
                );
    }

    /**
     * Resolves an existing Call.
     */
    private Call getCall(
            String callPublicId) {

        if (callPublicId == null
                || callPublicId.isBlank()) {

            throw new IllegalArgumentException(
                    CALL_NOT_FOUND
            );
        }

        return callRepository
                .findByPublicIdAndIsDeleted(
                        callPublicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                CALL_NOT_FOUND
                        )
                );
    }

    /**
     * Validates outbound call input before accessing
     * the database.
     */
    private void validateOutboundRequest(
            PlaceOutboundCallRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Outbound call request is required."
            );
        }

        if (request.getCallPublicId() == null
                || request
                .getCallPublicId()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Call public ID is required."
            );
        }

        if (request.getFromNumber() == null
                || request
                .getFromNumber()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "From number is required."
            );
        }

        if (request.getToNumber() == null
                || request
                .getToNumber()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "To number is required."
            );
        }
    }
}