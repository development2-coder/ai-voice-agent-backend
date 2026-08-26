package com.infinitio.aivoiceplatform.master.rolepermission.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import com.infinitio.aivoiceplatform.master.permission.validator.PermissionValidator;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.validator.RoleValidator;
import com.infinitio.aivoiceplatform.master.rolepermission.constant.RolePermissionConstants;
import com.infinitio.aivoiceplatform.master.rolepermission.constant.RolePermissionMessages;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.CreateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.UpdateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.entity.RolePermission;
import com.infinitio.aivoiceplatform.master.rolepermission.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for Role Permission mappings.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RolePermissionValidator {

    private static final Integer NOT_DELETED =
            RolePermissionConstants.NOT_DELETED;

    private final RolePermissionRepository rolePermissionRepository;

    private final RoleValidator roleValidator;

    private final PermissionValidator permissionValidator;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateRolePermissionRequest request) {

        log.info(
                "Validating Role Permission create request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Role permission request is required."
            );
        }

        if (!hasText(
                request.getRolePublicId()
        )) {

            throw new BadRequestException(
                    "Role public ID is required."
            );
        }

        if (!hasText(
                request.getPermissionPublicId()
        )) {

            throw new BadRequestException(
                    "Permission public ID is required."
            );
        }

        Role role;

        try {

            role =
                    roleValidator.validateAndGet(
                            request
                                    .getRolePublicId()
                                    .trim()
                    );

        } catch (ResourceNotFoundException exception) {

            throw new ResourceNotFoundException(
                    RolePermissionMessages.INVALID_ROLE
            );
        }

        Permission permission;

        try {

            permission =
                    permissionValidator.validateAndGet(
                            request
                                    .getPermissionPublicId()
                                    .trim()
                    );

        } catch (ResourceNotFoundException exception) {

            throw new ResourceNotFoundException(
                    RolePermissionMessages.INVALID_PERMISSION
            );
        }

        if (rolePermissionRepository
                .existsByRole_IdAndPermission_IdAndIsDeleted(
                        role.getId(),
                        permission.getId(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    RolePermissionMessages.ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateRolePermissionRequest request) {

        log.info(
                "Validating Role Permission update request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Role permission request is required."
            );
        }

        if (!hasText(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "Role permission public ID is required."
            );
        }

        if (!hasText(
                request.getRolePublicId()
        )) {

            throw new BadRequestException(
                    "Role public ID is required."
            );
        }

        if (!hasText(
                request.getPermissionPublicId()
        )) {

            throw new BadRequestException(
                    "Permission public ID is required."
            );
        }

        RolePermission existing =
                validateAndGet(
                        request
                                .getPublicId()
                                .trim()
                );

        Role role;

        try {

            role =
                    roleValidator.validateAndGet(
                            request
                                    .getRolePublicId()
                                    .trim()
                    );

        } catch (ResourceNotFoundException exception) {

            throw new ResourceNotFoundException(
                    RolePermissionMessages.INVALID_ROLE
            );
        }

        Permission permission;

        try {

            permission =
                    permissionValidator.validateAndGet(
                            request
                                    .getPermissionPublicId()
                                    .trim()
                    );

        } catch (ResourceNotFoundException exception) {

            throw new ResourceNotFoundException(
                    RolePermissionMessages.INVALID_PERMISSION
            );
        }

        if (rolePermissionRepository
                .existsByRole_IdAndPermission_IdAndIsDeletedAndIdNot(
                        role.getId(),
                        permission.getId(),
                        NOT_DELETED,
                        existing.getId()
                )) {

            throw new ConflictException(
                    RolePermissionMessages.ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // GET
    // =========================================================

    public RolePermission validateAndGet(
            String publicId) {

        if (!hasText(
                publicId
        )) {

            throw new BadRequestException(
                    "Role permission public ID is required."
            );
        }

        return rolePermissionRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                RolePermissionMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // STRING VALIDATION
    // =========================================================

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}