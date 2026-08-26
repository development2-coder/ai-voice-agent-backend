package com.infinitio.aivoiceplatform.organization.organizationaddress.mapper;

import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.CreateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.UpdateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.response.OrganizationAddressResponse;
import com.infinitio.aivoiceplatform.organization.organizationaddress.entity.OrganizationAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Organization Address Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface OrganizationAddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    OrganizationAddress toEntity(CreateOrganizationAddressRequest request);

    @Mapping(target = "organizationPublicId",
            source = "organization.publicId")
    @Mapping(target = "organizationName",
            source = "organization.organizationName")
    OrganizationAddressResponse toResponse(OrganizationAddress entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntity(UpdateOrganizationAddressRequest request,
                      @MappingTarget OrganizationAddress entity);

}