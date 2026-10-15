package com.infinitio.aivoiceplatform.master.rolepermission.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.CreateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.UpdateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.response.RolePermissionResponse;
import java.util.List;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.SaveRolePermissionsRequest;

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

    List<RolePermissionResponse> getAll();

    List<RolePermissionResponse> getByRolePublicId(
            String rolePublicId
    );

    List<RolePermissionResponse> saveForRole(
            String rolePublicId,
            SaveRolePermissionsRequest request
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