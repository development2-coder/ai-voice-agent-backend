package com.infinitio.aivoiceplatform.organization.organizationtype.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Organization Type Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTypeResponse {

    private String publicId;

    private String organizationTypeCode;

    private String organizationTypeName;

    private String description;

    private Integer displayOrder;

    private Integer isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}