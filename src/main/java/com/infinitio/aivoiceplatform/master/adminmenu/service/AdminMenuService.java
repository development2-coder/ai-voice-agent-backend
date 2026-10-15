package com.infinitio.aivoiceplatform.master.adminmenu.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.CreateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.UpdateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.response.AdminMenuResponse;

/**
 * Service for ADMIN menu management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface AdminMenuService {

    AdminMenuResponse create(
            CreateAdminMenuRequest request
    );

    AdminMenuResponse update(
            UpdateAdminMenuRequest request
    );

    AdminMenuResponse getByPublicId(
            String publicId
    );

    PageResponse<AdminMenuResponse> getAll(
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