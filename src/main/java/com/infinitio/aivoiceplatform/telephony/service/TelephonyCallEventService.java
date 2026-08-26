package com.infinitio.aivoiceplatform.telephony.service;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;

/**
 * Service responsible for persisting normalized
 * telephony call events.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TelephonyCallEventService {

    /**
     * Persists a normalized provider event when it
     * has not already been stored.
     *
     * @param call associated call
     * @param event normalized event
     */
    void save(
            Call call,
            NormalizedCallEventDto event
    );
}