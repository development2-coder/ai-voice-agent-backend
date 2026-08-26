package com.infinitio.aivoiceplatform.master.platformmodule.mapper;

import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.CreatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.UpdatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.response.PlatformModuleResponse;
import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for Platform Module.
 *
 * Responsible for converting between
 * Platform Module request DTOs, entity and response DTO.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface PlatformModuleMapper {

    // =========================================================
    // CREATE REQUEST -> ENTITY
    // =========================================================

    /**
     * Converts CreatePlatformModuleRequest to PlatformModule entity.
     *
     * System generated fields are ignored.
     *
     * isActive is intentionally ignored because create request
     * does not contain isActive. BaseEntity initializes it to 1
     * during @PrePersist.
     *
     * @param request create request
     * @return PlatformModule entity
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
    PlatformModule toEntity(
            CreatePlatformModuleRequest request
    );


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    /**
     * Converts PlatformModule entity to response DTO.
     *
     * The entity uses Integer flags:
     *
     * isSystem
     * isVisible
     * isActive
     *
     * while the response exposes boolean values:
     *
     * system
     * visible
     * active
     *
     * @param entity PlatformModule entity
     * @return PlatformModuleResponse
     */
    @Mapping(
            target = "system",
            expression = "java(toBoolean(entity.getIsSystem()))"
    )
    @Mapping(
            target = "visible",
            expression = "java(toBoolean(entity.getIsVisible()))"
    )
    @Mapping(
            target = "active",
            expression = "java(toBoolean(entity.getIsActive()))"
    )
    PlatformModuleResponse toResponse(
            PlatformModule entity
    );


    // =========================================================
    // UPDATE REQUEST -> ENTITY
    // =========================================================

    /**
     * Updates an existing PlatformModule entity from
     * UpdatePlatformModuleRequest.
     *
     * Only non-null request properties are copied.
     *
     * System generated and immutable fields are ignored.
     *
     * @param request update request
     * @param entity existing entity
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
    void updateEntityFromRequest(
            UpdatePlatformModuleRequest request,
            @MappingTarget PlatformModule entity
    );


    // =========================================================
    // INTEGER -> BOOLEAN
    // =========================================================

    /**
     * Converts integer flag to boolean.
     *
     * 1 = true
     * 0 = false
     *
     * Null is treated as false.
     *
     * @param value integer flag
     * @return boolean value
     */
    default boolean toBoolean(
            Integer value) {

        return value != null
                && value == 1;
    }
}