package com.infinitio.aivoiceplatform.master.rolepermission.mapper;

import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.CreateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.UpdateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.response.RolePermissionResponse;
import com.infinitio.aivoiceplatform.master.rolepermission.entity.RolePermission;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for Role Permission.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring"
)
public interface RolePermissionMapper {


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
            target = "role",
            ignore = true
    )
    @Mapping(
            target = "permission",
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
            target = "isActive",
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
    RolePermission toEntity(
            CreateRolePermissionRequest request
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
            target = "role",
            ignore = true
    )
    @Mapping(
            target = "permission",
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
            target = "isActive",
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
            UpdateRolePermissionRequest request,
            @MappingTarget RolePermission entity
    );


    // =========================================================
    // RESPONSE
    // =========================================================

    @Mapping(
            target = "rolePublicId",
            source = "role.publicId"
    )
    @Mapping(
            target = "roleCode",
            source = "role.roleCode"
    )
    @Mapping(
            target = "roleName",
            source = "role.roleName"
    )
    @Mapping(
            target = "permissionPublicId",
            source = "permission.publicId"
    )
    @Mapping(
            target = "permissionCode",
            source = "permission.permissionCode"
    )
    @Mapping(
            target = "permissionName",
            source = "permission.permissionName"
    )
    RolePermissionResponse toResponse(
            RolePermission entity
    );
}