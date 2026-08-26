package com.infinitio.aivoiceplatform.organization.organizationbranding.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationBrandingResponse {

    private String publicId;

    private String organizationPublicId;

    private String organizationName;

    private String logoUrl;

    private String faviconUrl;

    private String primaryColor;

    private String secondaryColor;

    private String accentColor;

    private Integer isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}