package com.infinitio.aivoiceplatform.organization.tenant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantRequest {

    @NotBlank(message = "Tenant public ID is required.")
    private String publicId;

    @NotBlank(message = "Organization is required.")
    private String organizationPublicId;

    @NotBlank(message = "Tenant code is required.")
    @Size(max = 50)
    private String tenantCode;

    @NotBlank(message = "Tenant name is required.")
    @Size(max = 150)
    private String tenantName;

    @Size(max = 150)
    private String displayName;

    @Size(max = 100)
    private String subdomain;

    @Email(message = "Invalid email.")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phoneNumber;

    private Boolean isDefault;

    private Boolean isActive;
}