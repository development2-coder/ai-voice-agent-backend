package com.infinitio.aivoiceplatform.master.rolemenu.mapper;

import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.CreateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.UpdateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.response.RoleMenuResponse;
import com.infinitio.aivoiceplatform.master.rolemenu.entity.RoleMenu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Role Menu Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring"
)
public interface RoleMenuMapper {


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
            target = "menu",
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
            target = "deletedAt",
            ignore = true
    )
    RoleMenu toEntity(
            CreateRoleMenuRequest request
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
            target = "menu",
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
            target = "deletedAt",
            ignore = true
    )
    void updateEntity(
            UpdateRoleMenuRequest request,
            @MappingTarget RoleMenu entity
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
            target = "menuPublicId",
            source = "menu.publicId"
    )
    @Mapping(
            target = "menuCode",
            source = "menu.menuCode"
    )
    @Mapping(
            target = "menuName",
            source = "menu.menuName"
    )
    @Mapping(
            target = "route",
            source = "menu.route"
    )
    RoleMenuResponse toResponse(
            RoleMenu entity
    );
}