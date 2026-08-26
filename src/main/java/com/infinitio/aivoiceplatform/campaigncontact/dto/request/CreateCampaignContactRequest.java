package com.infinitio.aivoiceplatform.campaigncontact.dto.request;

import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Create Campaign Contact Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignContactRequest {

    @NotBlank(
            message =
                    CampaignContactMessages.CAMPAIGN_REQUIRED
    )
    private String campaignPublicId;

    @Size(
            max = CampaignContactConstants.NAME_MAX_LENGTH
    )
    private String name;

    @NotBlank(
            message =
                    CampaignContactMessages.PHONE_NUMBER_REQUIRED
    )
    @Size(
            max =
                    CampaignContactConstants
                            .PHONE_NUMBER_MAX_LENGTH
    )
    private String phoneNumber;

    @Size(
            max =
                    CampaignContactConstants
                            .EXTERNAL_REFERENCE_MAX_LENGTH
    )
    private String externalReference;

    private Integer priority;

    @Size(
            max =
                    CampaignContactConstants
                            .DESCRIPTION_MAX_LENGTH
    )
    private String description;

    /**
     * Campaign-specific JSON data.
     */
    private String customData;
}