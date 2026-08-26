package com.infinitio.aivoiceplatform.organization.organizationaddress.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.CreateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.UpdateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.response.OrganizationAddressResponse;

public interface OrganizationAddressService {

    OrganizationAddressResponse create(CreateOrganizationAddressRequest request);

    OrganizationAddressResponse update(UpdateOrganizationAddressRequest request);

    OrganizationAddressResponse getByPublicId(String publicId);

    PageResponse<OrganizationAddressResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);

}