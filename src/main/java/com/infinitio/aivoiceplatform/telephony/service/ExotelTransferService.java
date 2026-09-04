package com.infinitio.aivoiceplatform.telephony.service;

import com.infinitio.aivoiceplatform.telephony.dto.response.ExotelTransferResponseDto;

/**
 * Provides application-side transfer routing information
 * required by Exotel during call handoff.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ExotelTransferService {

    /**
     * Resolves the transfer destination for an Exotel call.
     *
     * @param providerCallId Exotel CallSid
     * @return transfer routing response
     */
    ExotelTransferResponseDto getTransferDestination(
            String providerCallId
    );
}