package com.infinitio.aivoiceplatform.user.mapper;

import com.infinitio.aivoiceplatform.user.dto.request.CreateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.request.UpdateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.response.UserResponse;
import com.infinitio.aivoiceplatform.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * User Mapper.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    // =========================================================
    // CREATE
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "role", ignore = true)

    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "password", ignore = true)

    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "mobileVerified", ignore = true)
    User toEntity(
            CreateUserRequest request
    );


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    @Mapping(
            target = "tenantPublicId",
            source = "tenant.publicId"
    )
    @Mapping(
            target = "organizationPublicId",
            source = "organization.publicId"
    )
    @Mapping(
            target = "rolePublicId",
            source = "role.publicId"
    )
    UserResponse toResponse(
            User entity
    );


    // =========================================================
    // UPDATE
    // =========================================================

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "role", ignore = true)

    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "password", ignore = true)

    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "mobileVerified", ignore = true)
    void updateEntity(
            UpdateUserRequest request,
            @MappingTarget User entity
    );
}