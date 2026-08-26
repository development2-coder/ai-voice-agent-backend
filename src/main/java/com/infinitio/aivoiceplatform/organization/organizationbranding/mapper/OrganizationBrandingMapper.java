package com.infinitio.aivoiceplatform.organization.organizationbranding.mapper;

import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.CreateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.UpdateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.response.OrganizationBrandingResponse;
import com.infinitio.aivoiceplatform.organization.organizationbranding.entity.OrganizationBranding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrganizationBrandingMapper {

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
    OrganizationBranding toEntity(CreateOrganizationBrandingRequest request);

    @Mapping(target = "organizationPublicId",
            source = "organization.publicId")
    @Mapping(target = "organizationName",
            source = "organization.organizationName")
    OrganizationBrandingResponse toResponse(OrganizationBranding entity);

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
    void updateEntity(UpdateOrganizationBrandingRequest request,
                      @MappingTarget OrganizationBranding entity);

}