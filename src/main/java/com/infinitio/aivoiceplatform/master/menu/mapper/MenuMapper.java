package com.infinitio.aivoiceplatform.master.menu.mapper;

import com.infinitio.aivoiceplatform.master.menu.dto.request.CreateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.request.UpdateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.response.MenuResponse;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Menu Mapper.
 *
 * Maps Menu request DTOs to entities and Menu entities
 * to response DTOs.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface MenuMapper {

    // =========================================================
    // CREATE REQUEST -> ENTITY
    // =========================================================

    /**
     * Convert create request to Menu entity.
     *
     * Relationship fields are intentionally ignored because
     * they are resolved by the service layer.
     */
    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "module",
            ignore = true
    )
    @Mapping(
            target = "parentMenu",
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
    Menu toEntity(
            CreateMenuRequest request
    );


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    /**
     * Convert Menu entity to response DTO.
     *
     * Relationship public IDs are mapped explicitly.
     */
    @Mapping(
            target = "modulePublicId",
            source = "module.publicId"
    )
    @Mapping(
            target = "parentMenuPublicId",
            source = "parentMenu.publicId"
    )
    MenuResponse toResponse(
            Menu entity
    );


    // =========================================================
    // UPDATE REQUEST -> ENTITY
    // =========================================================

    /**
     * Update an existing Menu entity.
     *
     * Null request values are ignored.
     *
     * System/audit fields and relationships are controlled
     * by the service layer.
     */
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
            target = "module",
            ignore = true
    )
    @Mapping(
            target = "parentMenu",
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
            UpdateMenuRequest request,
            @MappingTarget Menu entity
    );
}