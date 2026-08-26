package com.infinitio.aivoiceplatform.organization.organization.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.CreateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.UpdateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.response.OrganizationResponse;

/**
 * Organization Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface OrganizationService {

    OrganizationResponse create(
            CreateOrganizationRequest request
    );

    OrganizationResponse update(
            UpdateOrganizationRequest request
    );

    OrganizationResponse getByPublicId(
            String publicId
    );

    PageResponse<OrganizationResponse> getAll(
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