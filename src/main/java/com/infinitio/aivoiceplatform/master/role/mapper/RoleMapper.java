package com.infinitio.aivoiceplatform.master.role.mapper;

import com.infinitio.aivoiceplatform.master.role.dto.request.CreateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.request.UpdateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.response.RoleResponse;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Role Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring"
)
public interface RoleMapper {

    // =========================================================
    // CREATE
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
    Role toEntity(
            CreateRoleRequest request
    );


    // =========================================================
    // UPDATE
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
    void updateEntity(
            UpdateRoleRequest request,
            @MappingTarget Role entity
    );


    // =========================================================
    // RESPONSE
    // =========================================================

    RoleResponse toResponse(
            Role entity
    );
}