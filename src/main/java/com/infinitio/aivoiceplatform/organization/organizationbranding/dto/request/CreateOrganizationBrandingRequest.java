package com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request;

import com.infinitio.aivoiceplatform.organization.organizationbranding.constant.OrganizationBrandingConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationBrandingRequest {

    @NotBlank(message = "Organization Public Id is required.")
    private String organizationPublicId;

    @Size(max = OrganizationBrandingConstants.URL_MAX_LENGTH)
    private String logoUrl;

    @Size(max = OrganizationBrandingConstants.URL_MAX_LENGTH)
    private String faviconUrl;

    @Size(max = OrganizationBrandingConstants.COLOR_MAX_LENGTH)
    private String primaryColor;

    @Size(max = OrganizationBrandingConstants.COLOR_MAX_LENGTH)
    private String secondaryColor;

    @Size(max = OrganizationBrandingConstants.COLOR_MAX_LENGTH)
    private String accentColor;

}