package com.infinitio.aivoiceplatform.organization.tenant.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.CreateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.UpdateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.response.TenantResponse;

public interface TenantService {

    TenantResponse create(CreateTenantRequest request);

    TenantResponse update(UpdateTenantRequest request);

    TenantResponse getByPublicId(String publicId);

    PageResponse<TenantResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}