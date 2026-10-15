package com.infinitio.aivoiceplatform.master.adminmenu.mapper;

import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.CreateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.UpdateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.response.AdminMenuResponse;
import com.infinitio.aivoiceplatform.master.adminmenu.entity.AdminMenu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for AdminMenu.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface AdminMenuMapper {

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "admin",
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
    AdminMenu toEntity(
            CreateAdminMenuRequest request
    );

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
            target = "admin",
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
            UpdateAdminMenuRequest request,
            @MappingTarget AdminMenu entity
    );

    @Mapping(
            target = "adminPublicId",
            source = "admin.publicId"
    )
    @Mapping(
            target = "adminName",
            source = "admin.tenantName"
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
    @Mapping(
            target = "icon",
            source = "menu.icon"
    )
    @Mapping(
            target = "displayOrder",
            source = "menu.displayOrder"
    )
    AdminMenuResponse toResponse(
            AdminMenu entity
    );
}