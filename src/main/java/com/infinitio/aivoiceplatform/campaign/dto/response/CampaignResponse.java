package com.infinitio.aivoiceplatform.campaign.dto.response;

import lombok.*;

/**
 * Campaign Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private String publicId;

    private String agentPublicId;

    private String phoneNumberPublicId;

    private String campaignCode;

    private String campaignName;

    private String campaignType;

    private String status;

    private String description;

    private Integer isActive;
}