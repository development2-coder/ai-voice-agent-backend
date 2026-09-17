package com.infinitio.aivoiceplatform.campaigncontact.dto.request;

import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelPreviewRowResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request used to confirm Campaign Contact Excel upload
 * after preview corrections.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactExcelConfirmRequest {

    @NotBlank
    private String campaignPublicId;

    @Valid
    @NotEmpty
    private List<CampaignContactExcelPreviewRowResponse> rows;
}