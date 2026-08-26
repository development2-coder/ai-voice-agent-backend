package com.infinitio.aivoiceplatform.master.role.repository;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.role.dto.request.CreateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.request.UpdateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.response.RoleResponse;

/**
 * Role Repository Custom.
 *
 * Contains all Stored Procedure operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RoleRepositoryCustom {

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
    PageResponse<RoleResponse> getAll(
            Integer page,
            Integer size);

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