package com.infinitio.aivoiceplatform.telephony.service;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;

/**
 * Service responsible for managing the lifecycle state
 * of the platform Call entity.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TelephonyCallStateService {

    /**
     * Initializes a Call before an outbound provider request.
     *
     * @param call call entity
     * @param providerCode provider code
     * @param fromNumber caller number
     * @param toNumber destination number
     */
    void initializeOutboundCall(
            Call call,
            String providerCode,
            String fromNumber,
            String toNumber
    );

    /**
     * Updates a Call using the provider response.
     *
     * @param call call entity
     * @param response provider response
     */
    void updateFromProviderResponse(
            Call call,
            ProviderCallResponseDto response
    );

    /**
     * Updates a Call using a normalized webhook event.
     *
     * @param call call entity
     * @param event normalized provider event
     */
    void updateFromWebhookEvent(
            Call call,
            NormalizedCallEventDto event
    );

    /**
     * Marks a Call as failed.
     *
     * @param call call entity
     * @param reason failure reason
     */
    void markFailed(
            Call call,
            String reason
    );
}