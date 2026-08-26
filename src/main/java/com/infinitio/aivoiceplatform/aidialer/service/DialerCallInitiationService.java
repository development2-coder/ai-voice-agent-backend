package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;


public interface DialerCallInitiationService {

    /**
     * Initiates a queued call through the configured
     * telephony provider.
     */
    DialerCallResponse initiateCall(
            String dialerCallPublicId
    );


}