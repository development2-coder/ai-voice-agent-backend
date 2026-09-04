package com.infinitio.aivoiceplatform.telephony.service;

/**
 * Provides provider-neutral operations for active telephony
 * media sessions.
 *
 * <p>
 * The implementation is responsible for interacting with the
 * provider-specific media transport, such as a WebSocket.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TelephonyMediaSessionService {

    /**
     * Closes the active media session associated with a call.
     *
     * @param providerCode telephony provider code
     * @param callId application call identifier
     * @param reason reason for closing the media session
     */
    void closeSession(
            String providerCode,
            String callId,
            String reason
    );
}