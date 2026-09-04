package com.infinitio.aivoiceplatform.telephony.service;

import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;

/**
 * Executes an active call transfer through the configured
 * telephony provider.
 *
 * <p>
 * This service is intentionally smaller than the general
 * {@link TelephonyService} so that Flow runtime execution can
 * trigger a transfer without creating a dependency cycle
 * with the broader telephony orchestration service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TelephonyTransferExecutionService {

    /**
     * Executes a call transfer through the selected provider.
     *
     * @param providerCode telephony provider code
     * @param request provider-neutral transfer request
     */
    void executeTransfer(
            String providerCode,
            TransferCallRequestDto request
    );
}