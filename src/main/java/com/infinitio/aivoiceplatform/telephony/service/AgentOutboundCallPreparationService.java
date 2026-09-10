package com.infinitio.aivoiceplatform.telephony.service;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceAgentOutboundCallRequestDto;

/**
 * Prepares persistent state required for an Agent outbound call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface AgentOutboundCallPreparationService {

    /**
     * Creates and commits the Call and CallSession required
     * before placing an outbound provider call.
     *
     * @param request outbound call request
     * @param flow selected Agent flow
     * @param providerCode telephony provider code
     * @param fromNumber configured caller number
     * @return persisted Call
     */
    Call prepareOutboundCall(
            PlaceAgentOutboundCallRequestDto request,
            Flow flow,
            String providerCode,
            String fromNumber
    );
}