package com.infinitio.aivoiceplatform.campaigncontact.service;

import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelPreviewResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Campaign Contact Excel preview processing.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignContactExcelPreviewService {

    /**
     * Generates an editable preview of Campaign Contact Excel data.
     *
     * <p>
     * The preview does not create database records. It returns
     * all non-empty Excel rows and identifies missing values.
     * </p>
     *
     * @param campaignPublicId campaign public identifier
     * @param file uploaded Excel file
     * @return preview response
     */
    CampaignContactExcelPreviewResponse preview(
            String campaignPublicId,
            MultipartFile file
    );
}