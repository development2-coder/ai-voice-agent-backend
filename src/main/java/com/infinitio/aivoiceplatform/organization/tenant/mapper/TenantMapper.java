package com.infinitio.aivoiceplatform.organization.tenant.mapper;

import com.infinitio.aivoiceplatform.organization.tenant.dto.request.CreateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.UpdateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.response.TenantResponse;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Tenant Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface TenantMapper {

    // =========================================================
    // REQUEST -> ENTITY
    // =========================================================

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "organization",
            ignore = true
    )
    @Mapping(
            target = "isActive",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "createdBy",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    @Mapping(
            target = "updatedBy",
            ignore = true
    )
    @Mapping(
            target = "isDeleted",
            ignore = true
    )
    @Mapping(
            target = "deletedAt",
            ignore = true
    )
    Tenant toEntity(
            CreateTenantRequest request
    );


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    @Mapping(
            target = "organizationPublicId",
            source = "organization.publicId"
    )
    TenantResponse toResponse(
            Tenant entity
    );


    // =========================================================
    // UPDATE REQUEST -> ENTITY
    // =========================================================

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "organization",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "createdBy",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    @Mapping(
            target = "updatedBy",
            ignore = true
    )
    @Mapping(
            target = "isDeleted",
            ignore = true
    )
    @Mapping(
            target = "deletedAt",
            ignore = true
    )
    @Mapping(
            target = "isActive",
            ignore = true
    )
    void updateEntity(
            UpdateTenantRequest request,
            @MappingTarget Tenant entity
    );
}