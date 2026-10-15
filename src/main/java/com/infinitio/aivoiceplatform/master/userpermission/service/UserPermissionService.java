package com.infinitio.aivoiceplatform.master.userpermission.service;

import com.infinitio.aivoiceplatform.master.userpermission.dto.response.UserPermissionResponse;

import java.util.List;

/**
 * Manages individual user permission assignments.
 */
public interface UserPermissionService {

    /**
     * Replaces the individual permissions assigned to a user.
     *
     * @param userPublicId target user's public ID
     * @param permissionPublicIds permission public IDs to assign
     * @return saved permission assignments
     */
    List<UserPermissionResponse> assignPermissions(
            String userPublicId,
            List<String> permissionPublicIds
    );

    /**
     * Gets the individual permissions assigned to a user.
     *
     * @param userPublicId target user's public ID
     * @return active permission assignments
     */
    List<UserPermissionResponse> getUserPermissions(
            String userPublicId
    );
}