package com.infinitio.aivoiceplatform.organization.organizationstatus.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.CreateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.UpdateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.response.OrganizationStatusResponse;

/**
 * Organization Status Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface OrganizationStatusService {

    OrganizationStatusResponse create(
            CreateOrganizationStatusRequest request
    );

    OrganizationStatusResponse update(
            UpdateOrganizationStatusRequest request
    );

    OrganizationStatusResponse getByPublicId(
            String publicId
    );

    PageResponse<OrganizationStatusResponse> getAll(
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