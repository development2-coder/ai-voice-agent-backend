package com.infinitio.aivoiceplatform.campaigncontact.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Represents one Campaign Contact Excel row during preview.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactExcelPreviewRowResponse {

    private Integer rowNumber;

    private Map<String, String> values;

    private List<String> missingFields;

    private boolean valid;
}