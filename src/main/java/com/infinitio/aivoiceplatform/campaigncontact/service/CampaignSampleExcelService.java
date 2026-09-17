package com.infinitio.aivoiceplatform.campaigncontact.service;

import org.springframework.core.io.Resource;

/**
 * Service for generating Campaign Contact sample Excel files.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignSampleExcelService {

    /**
     * Generates a sample Excel file containing standard
     * Campaign Contact columns and dynamic variables
     * extracted from the Campaign Flow.
     *
     * @param campaignPublicId campaign public identifier
     * @return generated Excel resource
     */
    Resource generateSampleExcel(
            String campaignPublicId
    );
}