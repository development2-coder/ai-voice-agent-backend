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
public class CreateTenantRequest {

    @NotBlank(message = "Organization is required.")
    private String organizationPublicId;

    @NotBlank(message = "Tenant code is required.")
    @Size(max = 50, message = "Tenant code cannot exceed 50 characters.")
    private String tenantCode;

    @NotBlank(message = "Tenant name is required.")
    @Size(max = 150, message = "Tenant name cannot exceed 150 characters.")
    private String tenantName;

    @Size(max = 150, message = "Display name cannot exceed 150 characters.")
    private String displayName;

    @Size(max = 100, message = "Subdomain cannot exceed 100 characters.")
    private String subdomain;

    @Email(message = "Invalid email.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters.")
    private String phoneNumber;

    private Boolean isDefault;

    private Boolean isActive;
}