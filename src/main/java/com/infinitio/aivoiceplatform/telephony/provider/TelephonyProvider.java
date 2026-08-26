package com.infinitio.aivoiceplatform.telephony.provider;

import com.infinitio.aivoiceplatform.telephony.dto.request.HangupCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.ProvisionNumberRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NumberResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;

/**
 * Provider abstraction for telephony operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TelephonyProvider {

    /**
     * Returns the provider code.
     *
     * @return provider code
     */
    String getProviderCode();

    /**
     * Places an outbound call.
     *
     * @param request outbound call request
     * @return provider call response
     */
    ProviderCallResponseDto placeOutboundCall(
            PlaceOutboundCallRequestDto request
    );

    /**
     * Provisions a telephony number.
     *
     * @param request number provisioning request
     * @return number response
     */
    NumberResponseDto provisionNumber(
            ProvisionNumberRequestDto request
    );

    /**
     * Transfers an active call.
     *
     * @param request transfer request
     */
    void transferCall(
            TransferCallRequestDto request
    );

    /**
     * Hangs up an active call.
     *
     * @param request hangup request
     */
    void hangupCall(
            HangupCallRequestDto request
    );

    /**
     * Normalizes a provider callback.
     *
     * @param payload provider callback payload
     * @return normalized call event
     */
    NormalizedCallEventDto normalizeCallEvent(
            String payload
    );
}