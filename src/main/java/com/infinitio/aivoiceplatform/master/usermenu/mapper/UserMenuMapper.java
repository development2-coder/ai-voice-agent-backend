package com.infinitio.aivoiceplatform.master.usermenu.mapper;

import com.infinitio.aivoiceplatform.master.usermenu.dto.request.CreateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.UpdateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.response.UserMenuResponse;
import com.infinitio.aivoiceplatform.master.usermenu.entity.UserMenu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * User Menu Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring"
)
public interface UserMenuMapper {

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
            target = "user",
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
    UserMenu toEntity(
            CreateUserMenuRequest request
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
            target = "user",
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
            UpdateUserMenuRequest request,
            @MappingTarget UserMenu entity
    );


    // =========================================================
    // RESPONSE
    // =========================================================

    @Mapping(
            target = "userPublicId",
            source = "user.publicId"
    )
    @Mapping(
            target = "username",
            source = "user.username"
    )
    @Mapping(
            target = "userName",
            source = "user.fullName"
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
    UserMenuResponse toResponse(
            UserMenu entity
    );
}