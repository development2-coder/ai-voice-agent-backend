package com.infinitio.aivoiceplatform.organization.organization.dto.request;

import com.infinitio.aivoiceplatform.organization.organization.constant.OrganizationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Update Organization Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {

    @NotBlank(message = "Organization public ID is required.")
    private String publicId;

    @NotBlank(message = "Organization code is required.")
    @Size(
            max = OrganizationConstants.CODE_MAX_LENGTH,
            message = "Organization code must not exceed 50 characters."
    )
    private String organizationCode;

    @NotBlank(message = "Organization name is required.")
    @Size(
            max = OrganizationConstants.NAME_MAX_LENGTH,
            message = "Organization name must not exceed 150 characters."
    )
    private String organizationName;

    @Size(
            max = OrganizationConstants.LEGAL_NAME_MAX_LENGTH,
            message = "Legal name must not exceed 200 characters."
    )
    private String legalName;

    @Email(message = "Invalid email address.")
    @Size(
            max = OrganizationConstants.EMAIL_MAX_LENGTH,
            message = "Email must not exceed 150 characters."
    )
    private String email;

    @Size(
            max = OrganizationConstants.MOBILE_MAX_LENGTH,
            message = "Mobile number must not exceed 20 characters."
    )
    private String mobileNumber;

    @Size(
            max = OrganizationConstants.WEBSITE_MAX_LENGTH,
            message = "Website must not exceed 255 characters."
    )
    private String website;

    @Size(
            max = OrganizationConstants.REGISTRATION_NUMBER_MAX_LENGTH,
            message = "Registration number must not exceed 100 characters."
    )
    private String registrationNumber;

    @Size(
            max = OrganizationConstants.TAX_IDENTIFICATION_NUMBER_MAX_LENGTH,
            message = "Tax identification number must not exceed 100 characters."
    )
    private String taxIdentificationNumber;

    @Size(
            max = OrganizationConstants.TIMEZONE_MAX_LENGTH,
            message = "Timezone must not exceed 50 characters."
    )
    private String timezone;

    @Size(
            max = OrganizationConstants.CURRENCY_MAX_LENGTH,
            message = "Currency must not exceed 20 characters."
    )
    private String currency;

    @Size(
            max = OrganizationConstants.DATE_FORMAT_MAX_LENGTH,
            message = "Date format must not exceed 20 characters."
    )
    private String dateFormat;

    @Size(
            max = OrganizationConstants.TIME_FORMAT_MAX_LENGTH,
            message = "Time format must not exceed 20 characters."
    )
    private String timeFormat;

    @Size(
            max = OrganizationConstants.LANGUAGE_MAX_LENGTH,
            message = "Language must not exceed 20 characters."
    )
    private String language;

    @NotBlank(message = "Organization type is required.")
    private String organizationTypePublicId;

    @NotBlank(message = "Organization status is required.")
    private String organizationStatusPublicId;
}