package com.infinitio.aivoiceplatform.organization.organization.mapper;

import com.infinitio.aivoiceplatform.organization.organization.dto.request.CreateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.UpdateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.response.OrganizationResponse;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Organization Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organizationType", ignore = true)
    @Mapping(target = "organizationStatus", ignore = true)
    Organization toEntity(
            CreateOrganizationRequest request
    );

    @Mapping(
            target = "organizationTypePublicId",
            source = "organizationType.publicId"
    )
    @Mapping(
            target = "organizationTypeCode",
            source = "organizationType.organizationTypeCode"
    )
    @Mapping(
            target = "organizationTypeName",
            source = "organizationType.organizationTypeName"
    )
    @Mapping(
            target = "organizationStatusPublicId",
            source = "organizationStatus.publicId"
    )
    @Mapping(
            target = "organizationStatusCode",
            source = "organizationStatus.organizationStatusCode"
    )
    @Mapping(
            target = "organizationStatusName",
            source = "organizationStatus.organizationStatusName"
    )
    OrganizationResponse toResponse(
            Organization organization
    );

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organizationType", ignore = true)
    @Mapping(target = "organizationStatus", ignore = true)
    void updateEntity(
            UpdateOrganizationRequest request,
            @MappingTarget Organization organization
    );
}