package com.infinitio.aivoiceplatform.telephony.mapper;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.entity.TelephonyCallEvent;
import org.springframework.stereotype.Component;

/**
 * Mapper for telephony call events.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
public class TelephonyCallMapper {

    /**
     * Converts a normalized provider event into a call event entity.
     *
     * @param event normalized call event
     * @param call associated call
     * @return telephony call event
     */
    public TelephonyCallEvent toEntity(
            NormalizedCallEventDto event,
            Call call) {

        return TelephonyCallEvent.builder()
                .call(call)
                .provider(
                        event.getProvider()
                )
                .providerCallId(
                        event.getProviderCallId()
                )
                .providerEventId(
                        event.getProviderEventId()
                )
                .event(
                        event.getEvent()
                )
                .fromNumber(
                        event.getFromNumber()
                )
                .toNumber(
                        event.getToNumber()
                )
                .eventAt(
                        event.getTimestamp() != null
                                ? event.getTimestamp()
                                .atZone(
                                        java.time.ZoneOffset.UTC
                                )
                                .toLocalDateTime()
                                : java.time.LocalDateTime.now()
                )
                .payload(
                        event.getPayload()
                )
                .build();
    }
}