package com.infinitio.aivoiceplatform.organization.organizationstatus.mapper;

import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.CreateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.UpdateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.response.OrganizationStatusResponse;
import com.infinitio.aivoiceplatform.organization.organizationstatus.entity.OrganizationStatus;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Organization Status Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface OrganizationStatusMapper {

    /**
     * Convert create request to entity.
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
    OrganizationStatus toEntity(
            CreateOrganizationStatusRequest request
    );

    /**
     * Convert entity to response.
     */
    OrganizationStatusResponse toResponse(
            OrganizationStatus entity
    );

    /**
     * Update entity from request.
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
            UpdateOrganizationStatusRequest request,
            @MappingTarget OrganizationStatus entity
    );
}