package com.infinitio.aivoiceplatform.organization.tenant.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a tenant and its initial ADMIN login.
 */
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

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters.")
    private String phoneNumber;

    @NotBlank(message = "Password is required.")
    @Size(
            min = 8,
            max = 500,
            message = "Password must be between 8 and 500 characters."
    )
    private String password;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;

    /**
     * Role assigned to the login account created for this tenant.
     * Tenant creation supports ADMIN only.
     *
     * Defaults to ADMIN in the service for backward compatibility
     * when older clients omit this field.
     */
    @Schema(
            description = "Role for the tenant login account. Only ADMIN is allowed.",
            example = "ADMIN",
            allowableValues = {"ADMIN"}
    )
    @Pattern(
            regexp = "(?i)ADMIN",
            message = "Tenant creation supports only the ADMIN role."
    )
    private String role;

    private Boolean isDefault;

    private Boolean isActive;
}