package com.infinitio.aivoiceplatform.master.rolepermission.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Update Role Permission Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRolePermissionRequest {

    @NotBlank(
            message = "Public ID is required."
    )
    private String publicId;

    @NotBlank(
            message = "Role is required."
    )
    private String rolePublicId;

    @NotBlank(
            message = "Permission is required."
    )
    private String permissionPublicId;
}