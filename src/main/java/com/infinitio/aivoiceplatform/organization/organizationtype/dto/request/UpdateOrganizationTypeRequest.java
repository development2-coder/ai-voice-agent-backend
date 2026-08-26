package com.infinitio.aivoiceplatform.organization.organizationtype.dto.request;

import com.infinitio.aivoiceplatform.organization.organizationtype.constant.OrganizationTypeConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Update Organization Type Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationTypeRequest {

    @NotBlank(
            message = "Organization type public ID is required."
    )
    private String publicId;

    @NotBlank(
            message = "Organization type code is required."
    )
    @Size(
            max = OrganizationTypeConstants.CODE_MAX_LENGTH,
            message = "Organization type code must not exceed 30 characters."
    )
    private String organizationTypeCode;

    @NotBlank(
            message = "Organization type name is required."
    )
    @Size(
            max = OrganizationTypeConstants.NAME_MAX_LENGTH,
            message = "Organization type name must not exceed 100 characters."
    )
    private String organizationTypeName;

    @Size(
            max = OrganizationTypeConstants.DESCRIPTION_MAX_LENGTH,
            message = "Description must not exceed 255 characters."
    )
    private String description;

    @Min(
            value = OrganizationTypeConstants.MIN_DISPLAY_ORDER,
            message = "Display order must be greater than zero."
    )
    private Integer displayOrder;
}