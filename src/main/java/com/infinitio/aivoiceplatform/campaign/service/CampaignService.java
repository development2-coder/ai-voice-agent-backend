package com.infinitio.aivoiceplatform.campaign.service;

import com.infinitio.aivoiceplatform.campaign.dto.request.CreateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.request.UpdateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;

/**
 * Service interface for Campaign.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignService {

    CampaignResponse create(
            CreateCampaignRequest request
    );

    CampaignResponse update(
            UpdateCampaignRequest request
    );

    CampaignResponse getByPublicId(
            String publicId
    );

    PageResponse<CampaignResponse> getAll(
            int page,
            int size
    );

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}