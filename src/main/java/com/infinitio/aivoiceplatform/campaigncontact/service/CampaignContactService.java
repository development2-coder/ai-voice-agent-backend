package com.infinitio.aivoiceplatform.campaigncontact.service;

import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.UpdateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactResponse;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelUploadResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Campaign Contact.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignContactService {

    CampaignContactResponse create(
            CreateCampaignContactRequest request
    );

    CampaignContactResponse update(
            UpdateCampaignContactRequest request
    );

    CampaignContactResponse getByPublicId(
            String publicId
    );

    PageResponse<CampaignContactResponse> getAll(
            int page,
            int size
    );

    PageResponse<CampaignContactResponse> getByCampaign(
            String campaignPublicId,
            int page,
            int size
    );

    CampaignContactResponse getNextEligibleContact(
            String campaignPublicId
    );

    CampaignContactResponse updateDialingStatus(
            String publicId,
            String status
    );

    CampaignContactResponse markDialing(
            String publicId
    );

    CampaignContactExcelUploadResponse uploadExcel(
            String campaignPublicId,
            MultipartFile file
    );

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}