package com.infinitio.aivoiceplatform.telephony.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response returned after starting a direct Agent outbound call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentOutboundCallResponseDto {

    private String callPublicId;

    private String providerCallId;

    private String provider;

    private String status;

    private String flowPublicId;

    private String agentPublicId;

    private String phoneNumberPublicId;

    private String fromNumber;

    private String toNumber;

    private String streamUrl;
}