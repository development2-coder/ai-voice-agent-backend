package com.infinitio.aivoiceplatform.campaign.service;

import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignVariablesResponse;

/**
 * Service for resolving dynamic variables used by
 * a Campaign's Flow.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignVariableService {

    /**
     * Extracts dynamic variables from the AI/LLM prompts
     * of the Flow associated with a Campaign.
     *
     * @param campaignPublicId campaign public identifier
     * @return campaign Flow variables
     */
    CampaignVariablesResponse getVariables(
            String campaignPublicId
    );
}