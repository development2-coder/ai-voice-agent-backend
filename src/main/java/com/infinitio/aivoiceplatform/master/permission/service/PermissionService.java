package com.infinitio.aivoiceplatform.master.permission.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.permission.dto.request.CreatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.dto.request.UpdatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse create(
            CreatePermissionRequest request
    );

    PermissionResponse update(
            UpdatePermissionRequest request
    );

    PermissionResponse getByPublicId(
            String publicId
    );

    List<PermissionResponse> getAll();

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}