package com.infinitio.aivoiceplatform.master.usermenu.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.CreateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.UpdateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.response.UserMenuResponse;

/**
 * User Menu Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface UserMenuService {

    UserMenuResponse create(
            CreateUserMenuRequest request
    );

    UserMenuResponse update(
            UpdateUserMenuRequest request
    );

    UserMenuResponse getByPublicId(
            String publicId
    );

    PageResponse<UserMenuResponse> getAll(
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