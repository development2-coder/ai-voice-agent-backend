package com.infinitio.aivoiceplatform.organization.organizationtype.mapper;

import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.CreateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.UpdateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.response.OrganizationTypeResponse;
import com.infinitio.aivoiceplatform.organization.organizationtype.entity.OrganizationType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Organization Type Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface OrganizationTypeMapper {

    /**
     * Convert Create Request to Entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    OrganizationType toEntity(
            CreateOrganizationTypeRequest request
    );

    /**
     * Convert Entity to Response.
     */
    OrganizationTypeResponse toResponse(
            OrganizationType entity
    );

    /**
     * Update Entity from Request.
     *
     * Null values are ignored.
     */
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
    void updateEntity(
            UpdateOrganizationTypeRequest request,
            @MappingTarget OrganizationType entity
    );
}