package com.infinitio.aivoiceplatform.campaigncontact.service;

import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CampaignContactExcelConfirmRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelPreviewResponse;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Campaign Contact Excel processing.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignContactExcelService {

    /**
     * Uploads Campaign Contacts from an Excel file.
     *
     * @param campaignPublicId campaign public identifier
     * @param file Excel file
     * @return upload result
     */
    CampaignContactExcelUploadResponse upload(
            String campaignPublicId,
            MultipartFile file
    );

    /**
     * Generates a preview of Campaign Contacts from
     * the uploaded Excel file.
     *
     * <p>
     * No database records are created during preview.
     * All rows are returned, including rows containing
     * missing values.
     * </p>
     *
     * @param campaignPublicId campaign public identifier
     * @param file Excel file
     * @return Excel preview response
     */
    CampaignContactExcelPreviewResponse preview(
            String campaignPublicId,
            MultipartFile file
    );

    /**
     * Confirms and saves Campaign Contacts after the
     * preview data has been corrected.
     *
     * @param request corrected Excel preview request
     * @return upload result
     */
    CampaignContactExcelUploadResponse confirm(
            CampaignContactExcelConfirmRequest request
    );
}