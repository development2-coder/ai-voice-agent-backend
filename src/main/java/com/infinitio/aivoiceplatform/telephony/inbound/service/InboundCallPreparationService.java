package com.infinitio.aivoiceplatform.telephony.inbound.service;

import com.infinitio.aivoiceplatform.call.entity.Call;

/**
 * Service responsible for preparing an inbound call for
 * AI Voice Agent processing.
 *
 * <p>
 * The service resolves the inbound Exotel number, associated
 * agent and active inbound flow, and creates the application
 * call context required by the existing Voice Gateway runtime.
 * </p>
 *
 * <p>
 * Existing outbound call preparation and runtime services are
 * intentionally not modified by this contract.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface InboundCallPreparationService {

    /**
     * Prepares an inbound call for AI Voice Agent processing.
     *
     * <p>
     * The operation must be idempotent for an already known
     * provider call identifier. If the provider call has already
     * been created, the existing application call should be
     * returned instead of creating a duplicate call.
     * </p>
     *
     * @param providerCode provider identifier such as EXOTEL
     * @param providerCallId unique call identifier received
     *                        from the telephony provider
     * @param callerNumber phone number of the caller
     * @param calledNumber phone number receiving the inbound call
     * @return prepared application call
     */
    Call prepareInboundCall(
            String providerCode,
            String providerCallId,
            String callerNumber,
            String calledNumber
    );
}