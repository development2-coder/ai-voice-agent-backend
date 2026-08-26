package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCollectedSlotRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Handles collected-slot operations for call sessions.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionSlotService {

    /**
     * Updates a collected slot.
     *
     * @param callId call identifier
     * @param request slot request
     * @return updated call session
     */
    CallSessionResponseDto updateCollectedSlot(
            String callId,
            UpdateCollectedSlotRequestDto request);
}