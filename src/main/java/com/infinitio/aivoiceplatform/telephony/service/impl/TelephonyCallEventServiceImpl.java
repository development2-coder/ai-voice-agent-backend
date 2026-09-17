package com.infinitio.aivoiceplatform.telephony.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.entity.TelephonyCallEvent;
import com.infinitio.aivoiceplatform.telephony.mapper.TelephonyCallMapper;
import com.infinitio.aivoiceplatform.telephony.repository.TelephonyCallEventRepository;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyCallEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation responsible for persisting normalized
 * telephony provider events.
 *
 * <p>
 * The audit user for a telephony event is inherited from
 * the associated Call because provider webhook requests may
 * not contain an authenticated application user.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelephonyCallEventServiceImpl
        implements TelephonyCallEventService {

    private final TelephonyCallEventRepository
            telephonyCallEventRepository;

    private final TelephonyCallMapper
            telephonyCallMapper;

    /**
     * Persists a normalized telephony provider event.
     *
     * <p>
     * The createdBy value is inherited from the associated
     * Call. The Call is created in the authenticated user
     * context, while the provider event is received later
     * through a webhook request.
     * </p>
     *
     * @param call associated call
     * @param event normalized provider event
     */
    @Override
    @Transactional
    public void save(
            Call call,
            NormalizedCallEventDto event) {

        if (call == null
                || event == null) {

            log.warn(
                    "Unable to persist telephony call event. "
                            + "Call or event is null."
            );

            return;
        }

        String providerEventId =
                event.getProviderEventId();

        if (providerEventId != null
                && !providerEventId.isBlank()
                && telephonyCallEventRepository
                .existsByProviderEventId(
                        providerEventId
                )) {

            log.debug(
                    "Telephony event already exists. "
                            + "providerEventId={}",
                    providerEventId
            );

            return;
        }

        Long createdBy =
                call.getCreatedBy();

        if (createdBy == null) {

            log.error(
                    "Unable to persist telephony call event. "
                            + "createdBy is null for callPublicId={}, "
                            + "providerCallId={}",
                    call.getPublicId(),
                    event.getProviderCallId()
            );

            throw new IllegalStateException(
                    "Unable to determine createdBy for telephony call event."
            );
        }

        TelephonyCallEvent callEvent =
                telephonyCallMapper.toEntity(
                        event,
                        call
                );

        /*
         * The provider webhook is a system-to-system request.
         * Therefore, inherit the audit user from the original
         * Call instead of reading the current SecurityContext.
         */
        callEvent.setCreatedBy(
                createdBy
        );

        telephonyCallEventRepository.save(
                callEvent
        );

        log.info(
                "Telephony call event persisted successfully. "
                        + "callPublicId={}, providerCallId={}, "
                        + "providerEventId={}, event={}, createdBy={}",
                call.getPublicId(),
                event.getProviderCallId(),
                event.getProviderEventId(),
                event.getEvent(),
                createdBy
        );
    }
}