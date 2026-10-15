package com.infinitio.aivoiceplatform.master.userpermission.dto.response;

/**
 * Permission assigned directly to a user.
 */
public record UserPermissionResponse(
        String permissionPublicId,
        String permissionCode,
        String permissionName,
        String moduleName
) {
}