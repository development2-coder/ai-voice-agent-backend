package com.infinitio.aivoiceplatform.master.role.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.role.dto.request.CreateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.request.UpdateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.response.RoleResponse;

/**
 * Role Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RoleService {

    /**
     * Create Role.
     */
    RoleResponse create(CreateRoleRequest request);

    /**
     * Update Role.
     */
    RoleResponse update(UpdateRoleRequest request);

    /**
     * Get Role By Public Id.
     */
    RoleResponse getByPublicId(String publicId);

    /**
     * Get All Roles.
     */
    PageResponse<RoleResponse> getAll(int page, int size);

    /**
     * Delete Role.
     */
    void delete(String publicId);

    /**
     * Activate Role.
     */
    void activate(String publicId);

    /**
     * Deactivate Role.
     */
    void deactivate(String publicId);

}