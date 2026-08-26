package com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request;

import com.infinitio.aivoiceplatform.organization.organizationaddress.constant.OrganizationAddressConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationAddressRequest {

    @NotBlank(message = "Organization Public Id is required.")
    private String organizationPublicId;

    @NotBlank(message = "Address Line 1 is required.")
    @Size(max = OrganizationAddressConstants.ADDRESS_MAX_LENGTH)
    private String addressLine1;

    @Size(max = OrganizationAddressConstants.ADDRESS_MAX_LENGTH)
    private String addressLine2;

    @NotBlank(message = "City is required.")
    @Size(max = OrganizationAddressConstants.CITY_MAX_LENGTH)
    private String city;

    @NotBlank(message = "State is required.")
    @Size(max = OrganizationAddressConstants.STATE_MAX_LENGTH)
    private String state;

    @NotBlank(message = "Country is required.")
    @Size(max = OrganizationAddressConstants.COUNTRY_MAX_LENGTH)
    private String country;

    @Size(max = OrganizationAddressConstants.POSTAL_CODE_MAX_LENGTH)
    private String postalCode;

}