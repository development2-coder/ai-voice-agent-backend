package com.infinitio.aivoiceplatform.organization.tenant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Update Tenant Request.
 *
 * Password and confirm password are optional during update.
 * If password is supplied, confirm password must also be supplied
 * and both values must match.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
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
    @Size(
            max = 50,
            message = "Tenant code cannot exceed 50 characters."
    )
    private String tenantCode;

    @NotBlank(message = "Tenant name is required.")
    @Size(
            max = 150,
            message = "Tenant name cannot exceed 150 characters."
    )
    private String tenantName;

    @Size(
            max = 150,
            message = "Display name cannot exceed 150 characters."
    )
    private String displayName;

    @Size(
            max = 100,
            message = "Subdomain cannot exceed 100 characters."
    )
    private String subdomain;

    @Email(message = "Invalid email.")
    @Size(
            max = 150,
            message = "Email cannot exceed 150 characters."
    )
    private String email;

    @Size(
            max = 20,
            message = "Phone number cannot exceed 20 characters."
    )
    private String phoneNumber;

    /*
     * Password is optional during Tenant update.
     *
     * If provided, it will be used to update the
     * automatically-created ADMIN user's password.
     */
    @Size(
            min = 8,
            max = 500,
            message = "Password must be between 8 and 500 characters."
    )
    private String password;

    /*
     * Confirm password is optional during Tenant update.
     */
    private String confirmPassword;

    private Boolean isDefault;

    private Boolean isActive;
}