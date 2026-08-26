package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;

/**
 * Synchronizes AI Dialer call attempts with
 * normalized telephony provider events.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface DialerCallWebhookService {

    /**
     * Processes a normalized provider event and updates
     * the corresponding DialerCall when the provider
     * call belongs to an AI Dialer attempt.
     *
     * @param event normalized telephony event
     */
    void synchronize(
            NormalizedCallEventDto event
    );
}