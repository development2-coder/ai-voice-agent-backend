package com.infinitio.aivoiceplatform.campaigncontact.dto.response;

import lombok.*;

import java.util.List;

/**
 * Response for Campaign Contact Excel upload.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactExcelUploadResponse {

    private String campaignPublicId;

    private Integer totalRows;

    private Integer importedRows;

    private Integer failedRows;

    private List<String> errors;
}