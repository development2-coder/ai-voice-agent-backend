package com.infinitio.aivoiceplatform.campaign.dto.request;

import com.infinitio.aivoiceplatform.campaign.constant.CampaignConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Campaign Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampaignRequest {

    @NotBlank(message = "Public Id is required.")
    private String publicId;

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumberPublicId;

    @NotBlank(message = "Campaign code is required.")
    @Size(max = CampaignConstants.CAMPAIGN_CODE_MAX_LENGTH)
    private String campaignCode;

    @NotBlank(message = "Campaign name is required.")
    @Size(max = CampaignConstants.CAMPAIGN_NAME_MAX_LENGTH)
    private String campaignName;

    @NotBlank(message = "Campaign type is required.")
    @Size(max = CampaignConstants.CAMPAIGN_TYPE_MAX_LENGTH)
    private String campaignType;

    @Size(max = CampaignConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}