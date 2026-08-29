package com.infinitio.aivoiceplatform.telephony.service;

import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceAgentOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.AgentOutboundCallResponseDto;

public interface AgentOutboundCallService {

    AgentOutboundCallResponseDto placeAgentOutboundCall(
            PlaceAgentOutboundCallRequestDto request
    );
}