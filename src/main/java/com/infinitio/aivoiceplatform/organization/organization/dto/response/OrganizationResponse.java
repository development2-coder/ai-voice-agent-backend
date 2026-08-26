package com.infinitio.aivoiceplatform.organization.organization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Organization Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private String publicId;

    private String organizationCode;

    private String organizationName;

    private String legalName;

    private String email;

    private String mobileNumber;

    private String website;

    private String registrationNumber;

    private String taxIdentificationNumber;

    private String timezone;

    private String currency;

    private String dateFormat;

    private String timeFormat;

    private String language;

    private String organizationTypePublicId;

    private String organizationTypeCode;

    private String organizationTypeName;

    private String organizationStatusPublicId;

    private String organizationStatusCode;

    private String organizationStatusName;

    private Integer isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}