package com.infinitio.aivoiceplatform.telephony.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for placing a direct outbound call
 * using an Agent Flow.
 *
 * <p>
 * This request intentionally does not require a Campaign
 * or CampaignContact.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceAgentOutboundCallRequestDto {

    /**
     * Flow public ID that must execute during the call.
     */
    @NotBlank
    private String flowPublicId;

    /**
     * Public ID of the phone number assigned to the Agent.
     */
    @NotBlank
    private String phoneNumberPublicId;

    /**
     * Customer phone number.
     */
    @NotBlank
    private String toNumber;
}