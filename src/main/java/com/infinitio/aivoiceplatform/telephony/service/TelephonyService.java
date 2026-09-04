package com.infinitio.aivoiceplatform.telephony.service;

import com.infinitio.aivoiceplatform.telephony.dto.request.HangupCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.ProvisionNumberRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NumberResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.NumberSearchRequestDto;
import java.util.List;
/**
 * Provides provider-independent telephony operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TelephonyService {

    /**
     * Provisions a telephone number.
     *
     * @param providerCode telephony provider code
     * @param request number provisioning request
     * @return number response
     */
    NumberResponseDto provisionNumber(
            String providerCode,
            ProvisionNumberRequestDto request
    );

    /**
     * Places an outbound call.
     *
     * @param providerCode telephony provider code
     * @param request outbound call request
     * @return provider call response
     */
    ProviderCallResponseDto placeOutboundCall(
            String providerCode,
            PlaceOutboundCallRequestDto request
    );

    /**
     * Processes an inbound provider webhook.
     *
     * @param providerCode telephony provider code
     * @param webhookPayload provider webhook payload
     * @return normalized call event
     */
    NormalizedCallEventDto processInboundCall(
            String providerCode,
            String webhookPayload
    );

    /**
     * Transfers an active call.
     *
     * @param providerCode telephony provider code
     * @param request transfer request
     */
    void transferCall(
            String providerCode,
            TransferCallRequestDto request
    );

    /**
     * Hangs up an active call.
     *
     * @param providerCode telephony provider code
     * @param request hangup request
     */
    void hangupCall(
            String providerCode,
            HangupCallRequestDto request
    );

    /**
     * Retrieves numbers already owned by the provider account.
     *
     * @param providerCode provider identifier
     * @return owned numbers
     */
    List<NumberResponseDto> getOwnedNumbers(
            String providerCode
    );

    /**
     * Retrieves numbers available for provisioning.
     *
     * @param providerCode provider identifier
     * @param request number search criteria
     * @return available numbers
     */
    /**
     * Retrieves numbers available for provisioning.
     *
     * @param providerCode provider identifier
     * @param request provider-independent number search criteria
     * @return available numbers
     */
    List<NumberResponseDto> getAvailableNumbers(
            String providerCode,
            NumberSearchRequestDto request
    );
}