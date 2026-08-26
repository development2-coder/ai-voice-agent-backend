package com.infinitio.aivoiceplatform.campaigncontact.service;

import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Campaign Contact Excel processing.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignContactExcelService {

    CampaignContactExcelUploadResponse upload(
            String campaignPublicId,
            MultipartFile file
    );
}