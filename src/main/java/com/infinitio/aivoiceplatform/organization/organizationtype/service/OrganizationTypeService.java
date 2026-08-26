package com.infinitio.aivoiceplatform.organization.organizationtype.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.CreateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.UpdateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.response.OrganizationTypeResponse;

/**
 * Organization Type Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface OrganizationTypeService {

    OrganizationTypeResponse create(
            CreateOrganizationTypeRequest request
    );

    OrganizationTypeResponse update(
            UpdateOrganizationTypeRequest request
    );

    OrganizationTypeResponse getByPublicId(
            String publicId
    );

    PageResponse<OrganizationTypeResponse> getAll(
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