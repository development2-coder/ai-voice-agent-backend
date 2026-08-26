package com.infinitio.aivoiceplatform.organization.organizationaddress.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAddressResponse {

    private String publicId;

    private String organizationPublicId;

    private String organizationName;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private Integer isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}