package com.infinitio.aivoiceplatform.organization.organizationbranding.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.CreateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.UpdateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.response.OrganizationBrandingResponse;

public interface OrganizationBrandingService {

    OrganizationBrandingResponse create(CreateOrganizationBrandingRequest request);

    OrganizationBrandingResponse update(UpdateOrganizationBrandingRequest request);

    OrganizationBrandingResponse getByPublicId(String publicId);

    PageResponse<OrganizationBrandingResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);

}