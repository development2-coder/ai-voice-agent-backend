package com.infinitio.aivoiceplatform.campaigncontact.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents Campaign Contact Excel preview information.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactExcelPreviewResponse {

    private String campaignPublicId;

    private List<String> headers;

    private List<CampaignContactExcelPreviewRowResponse> rows;

    private Integer totalRows;

    private Integer validRows;

    private Integer invalidRows;

    private boolean readyForUpload;
}