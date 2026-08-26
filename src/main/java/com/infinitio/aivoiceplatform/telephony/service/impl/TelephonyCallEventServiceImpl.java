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

    @Override
    @Transactional
    public void save(
            Call call,
            NormalizedCallEventDto event) {

        if (call == null
                || event == null) {

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

        TelephonyCallEvent callEvent =
                telephonyCallMapper.toEntity(
                        event,
                        call
                );

        telephonyCallEventRepository.save(
                callEvent
        );

        log.debug(
                "Telephony call event persisted. "
                        + "providerCallId={}, providerEventId={}, event={}",
                event.getProviderCallId(),
                event.getProviderEventId(),
                event.getEvent()
        );
    }
}