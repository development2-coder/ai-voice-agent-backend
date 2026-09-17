package com.infinitio.aivoiceplatform.campaign.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response containing dynamic variables used by
 * the Flow associated with a Campaign.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignVariablesResponse {

    /**
     * Campaign public identifier.
     */
    private String campaignPublicId;

    /**
     * Agent public identifier.
     */
    private String agentPublicId;

    /**
     * Flow public identifier from which variables were extracted.
     */
    private String flowPublicId;

    /**
     * Dynamic variables found in AI/LLM node prompts.
     */
    private List<String> variables;
}