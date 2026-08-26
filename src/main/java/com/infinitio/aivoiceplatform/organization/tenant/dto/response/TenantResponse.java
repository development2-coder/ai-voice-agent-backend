package com.infinitio.aivoiceplatform.organization.tenant.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private String publicId;

    private String organizationPublicId;

    private String tenantCode;

    private String tenantName;

    private String displayName;

    private String subdomain;

    private String email;

    private String phoneNumber;

    private Boolean isDefault;

    private Integer isActive;
}