package com.infinitio.aivoiceplatform.master.rolepermission.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.CreateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.UpdateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.response.RolePermissionResponse;

/**
 * Role Permission Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RolePermissionService {

    RolePermissionResponse create(
            CreateRolePermissionRequest request
    );

    RolePermissionResponse update(
            UpdateRolePermissionRequest request
    );

    RolePermissionResponse getByPublicId(
            String publicId
    );

    PageResponse<RolePermissionResponse> getAll(
            int page,
            int size
    );

    void delete(
            String publicId
    );

    void activate(
            String publicId
    );

    void deactivate(
            String publicId
    );
}