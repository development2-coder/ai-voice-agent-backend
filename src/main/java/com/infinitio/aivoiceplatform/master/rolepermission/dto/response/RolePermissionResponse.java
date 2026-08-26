package com.infinitio.aivoiceplatform.master.rolepermission.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Role Permission Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionResponse {

    private String publicId;

    private String rolePublicId;

    private String roleCode;

    private String roleName;

    private String permissionPublicId;

    private String permissionCode;

    private String permissionName;

    private Integer isActive;
}