package com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request;

import com.infinitio.aivoiceplatform.organization.organizationstatus.constant.OrganizationStatusConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Update Organization Status Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationStatusRequest {

    @NotBlank(
            message = "Organization status public ID is required."
    )
    private String publicId;

    @NotBlank(
            message = "Organization status code is required."
    )
    @Size(
            max = OrganizationStatusConstants.CODE_MAX_LENGTH,
            message = "Organization status code must not exceed 30 characters."
    )
    private String organizationStatusCode;

    @NotBlank(
            message = "Organization status name is required."
    )
    @Size(
            max = OrganizationStatusConstants.NAME_MAX_LENGTH,
            message = "Organization status name must not exceed 100 characters."
    )
    private String organizationStatusName;

    @Size(
            max = OrganizationStatusConstants.DESCRIPTION_MAX_LENGTH,
            message = "Description must not exceed 255 characters."
    )
    private String description;

    @NotNull(
            message = "Display order is required."
    )
    @Min(
            value = OrganizationStatusConstants.MIN_DISPLAY_ORDER,
            message = "Display order must be greater than zero."
    )
    private Integer displayOrder;
}