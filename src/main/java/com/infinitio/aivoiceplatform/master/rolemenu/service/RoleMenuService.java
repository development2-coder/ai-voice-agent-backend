package com.infinitio.aivoiceplatform.master.rolemenu.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.CreateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.UpdateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.response.RoleMenuResponse;

/**
 * Role Menu Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RoleMenuService {

    RoleMenuResponse create(
            CreateRoleMenuRequest request
    );

    RoleMenuResponse update(
            UpdateRoleMenuRequest request
    );

    RoleMenuResponse getByPublicId(
            String publicId
    );

    PageResponse<RoleMenuResponse> getAll(
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