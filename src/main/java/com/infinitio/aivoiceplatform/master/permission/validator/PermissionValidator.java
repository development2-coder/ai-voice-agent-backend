package com.infinitio.aivoiceplatform.master.permission.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.permission.constant.PermissionConstants;
import com.infinitio.aivoiceplatform.master.permission.constant.PermissionMessages;
import com.infinitio.aivoiceplatform.master.permission.dto.request.CreatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.dto.request.UpdatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import com.infinitio.aivoiceplatform.master.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for Permission business validations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private static final Integer NOT_DELETED = 0;

    private final PermissionRepository permissionRepository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreatePermissionRequest request) {

        log.info(
                "Validating Permission create request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Permission request is required."
            );
        }

        validatePermissionCode(
                request.getPermissionCode()
        );

        validatePermissionName(
                request.getPermissionName()
        );

        validateDescription(
                request.getDescription()
        );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        validateIsSystem(
                request.getIsSystem()
        );

        String permissionCode =
                normalize(
                        request.getPermissionCode()
                );

        String permissionName =
                normalize(
                        request.getPermissionName()
                );

        /*
         * Only non-deleted permissions participate
         * in duplicate validation.
         */
        if (permissionRepository
                .existsByPermissionCodeAndIsDeleted(
                        permissionCode,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    PermissionMessages.CODE_ALREADY_EXISTS
            );
        }

        if (permissionRepository
                .existsByPermissionNameAndIsDeleted(
                        permissionName,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    PermissionMessages.NAME_ALREADY_EXISTS
            );
        }

        log.info(
                "Permission create validation completed."
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdatePermissionRequest request) {

        log.info(
                "Validating Permission update request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Permission request is required."
            );
        }

        if (!hasText(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "Permission public ID is required."
            );
        }

        validatePermissionCode(
                request.getPermissionCode()
        );

        validatePermissionName(
                request.getPermissionName()
        );

        validateDescription(
                request.getDescription()
        );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        validateIsSystem(
                request.getIsSystem()
        );

        String publicId =
                request.getPublicId().trim();

        Permission existingPermission =
                validateAndGet(
                        publicId
                );

        String permissionCode =
                normalize(
                        request.getPermissionCode()
                );

        String permissionName =
                normalize(
                        request.getPermissionName()
                );

        /*
         * Check duplicate code while excluding
         * the current permission.
         */
        if (!equals(
                existingPermission.getPermissionCode(),
                permissionCode
        )) {

            if (permissionRepository
                    .existsByPermissionCodeAndIsDeletedAndPublicIdNot(
                            permissionCode,
                            NOT_DELETED,
                            publicId
                    )) {

                throw new ConflictException(
                        PermissionMessages.CODE_ALREADY_EXISTS
                );
            }
        }

        /*
         * Check duplicate name while excluding
         * the current permission.
         */
        if (!equals(
                existingPermission.getPermissionName(),
                permissionName
        )) {

            if (permissionRepository
                    .existsByPermissionNameAndIsDeletedAndPublicIdNot(
                            permissionName,
                            NOT_DELETED,
                            publicId
                    )) {

                throw new ConflictException(
                        PermissionMessages.NAME_ALREADY_EXISTS
                );
            }
        }

        log.info(
                "Permission update validation completed."
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    public Permission validateAndGet(
            String publicId) {

        if (!hasText(
                publicId
        )) {

            throw new BadRequestException(
                    "Permission public ID is required."
            );
        }

        return permissionRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PermissionMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // PERMISSION CODE
    // =========================================================

    private void validatePermissionCode(
            String permissionCode) {

        if (!hasText(
                permissionCode
        )) {

            throw new BadRequestException(
                    "Permission code is required."
            );
        }

        String value =
                permissionCode.trim();

        if (value.length() >
                PermissionConstants
                        .PERMISSION_CODE_MAX_LENGTH) {

            throw new BadRequestException(
                    "Permission code must not exceed "
                            + PermissionConstants
                            .PERMISSION_CODE_MAX_LENGTH
                            + " characters."
            );
        }

        /*
         * Keep permission codes predictable.
         */
        if (!value.matches(
                "^[A-Za-z0-9_-]+$"
        )) {

            throw new BadRequestException(
                    "Permission code may contain only "
                            + "letters, numbers, underscore and hyphen."
            );
        }
    }


    // =========================================================
    // PERMISSION NAME
    // =========================================================

    private void validatePermissionName(
            String permissionName) {

        if (!hasText(
                permissionName
        )) {

            throw new BadRequestException(
                    "Permission name is required."
            );
        }

        String value =
                permissionName.trim();

        if (value.length() >
                PermissionConstants
                        .PERMISSION_NAME_MAX_LENGTH) {

            throw new BadRequestException(
                    "Permission name must not exceed "
                            + PermissionConstants
                            .PERMISSION_NAME_MAX_LENGTH
                            + " characters."
            );
        }
    }


    // =========================================================
    // DESCRIPTION
    // =========================================================

    private void validateDescription(
            String description) {

        if (description == null) {
            return;
        }

        if (description.length() >
                PermissionConstants
                        .DESCRIPTION_MAX_LENGTH) {

            throw new BadRequestException(
                    "Permission description must not exceed "
                            + PermissionConstants
                            .DESCRIPTION_MAX_LENGTH
                            + " characters."
            );
        }
    }


    // =========================================================
    // DISPLAY ORDER
    // =========================================================

    private void validateDisplayOrder(
            Integer displayOrder) {

        if (displayOrder == null) {
            return;
        }

        if (displayOrder < 1) {

            throw new BadRequestException(
                    "Display order must be greater than zero."
            );
        }
    }


    // =========================================================
    // SYSTEM FLAG
    // =========================================================

    private void validateIsSystem(
            Integer isSystem) {

        if (isSystem == null) {
            return;
        }

        if (!Integer.valueOf(
                PermissionConstants.SYSTEM_PERMISSION
        ).equals(isSystem)
                && !Integer.valueOf(
                PermissionConstants.CUSTOM_PERMISSION
        ).equals(isSystem)) {

            throw new BadRequestException(
                    "isSystem must be either 0 or 1."
            );
        }
    }


    // =========================================================
    // NORMALIZE
    // =========================================================

    private String normalize(
            String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }


    // =========================================================
    // HAS TEXT
    // =========================================================

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }


    // =========================================================
    // EQUALS
    // =========================================================

    private boolean equals(
            String first,
            String second) {

        if (first == null
                && second == null) {

            return true;
        }

        if (first == null
                || second == null) {

            return false;
        }

        return first.equals(
                second
        );
    }
}