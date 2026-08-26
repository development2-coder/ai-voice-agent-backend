package com.infinitio.aivoiceplatform.master.role.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleMessages;
import com.infinitio.aivoiceplatform.master.role.dto.request.CreateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.request.UpdateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Role Validator.
 *
 * Performs business validations for Role operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleValidator {

    private static final Integer NOT_DELETED = 0;

    private final RoleRepository roleRepository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateRoleRequest request) {

        log.info(
                "Validating Role Create Request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Role request is required."
            );
        }

        validateRoleCode(
                request.getRoleCode()
        );

        validateRoleName(
                request.getRoleName()
        );

        validateDescription(
                request.getDescription()
        );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        validateSystemFlag(
                request.getIsSystem()
        );

        validateDefaultFlag(
                request.getIsDefault()
        );

        String roleCode =
                normalize(
                        request.getRoleCode()
                );

        String roleName =
                normalize(
                        request.getRoleName()
                );

        /*
         * Only non-deleted roles participate in
         * duplicate validation.
         */
        if (roleRepository
                .existsByRoleCodeAndIsDeleted(
                        roleCode,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    RoleMessages.ROLE_CODE_ALREADY_EXISTS
            );
        }

        if (roleRepository
                .existsByRoleNameAndIsDeleted(
                        roleName,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    RoleMessages.ROLE_NAME_ALREADY_EXISTS
            );
        }

        log.info(
                "Role Create validation completed."
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateRoleRequest request) {

        log.info(
                "Validating Role Update Request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Role request is required."
            );
        }

        if (!hasText(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "Role public ID is required."
            );
        }

        validateRoleCode(
                request.getRoleCode()
        );

        validateRoleName(
                request.getRoleName()
        );

        validateDescription(
                request.getDescription()
        );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        validateSystemFlag(
                request.getIsSystem()
        );

        validateDefaultFlag(
                request.getIsDefault()
        );

        Role existingRole =
                validateAndGet(
                        request.getPublicId().trim()
                );

        String roleCode =
                normalize(
                        request.getRoleCode()
                );

        String roleName =
                normalize(
                        request.getRoleName()
                );

        /*
         * Check duplicate role code excluding
         * current record.
         */
        if (!equals(
                existingRole.getRoleCode(),
                roleCode
        )) {

            if (roleRepository
                    .existsByRoleCodeAndIsDeletedAndPublicIdNot(
                            roleCode,
                            NOT_DELETED,
                            request.getPublicId().trim()
                    )) {

                throw new ConflictException(
                        RoleMessages.ROLE_CODE_ALREADY_EXISTS
                );
            }
        }

        /*
         * Check duplicate role name excluding
         * current record.
         */
        if (!equals(
                existingRole.getRoleName(),
                roleName
        )) {

            if (roleRepository
                    .existsByRoleNameAndIsDeletedAndPublicIdNot(
                            roleName,
                            NOT_DELETED,
                            request.getPublicId().trim()
                    )) {

                throw new ConflictException(
                        RoleMessages.ROLE_NAME_ALREADY_EXISTS
                );
            }
        }

        log.info(
                "Role Update validation completed."
        );
    }


    // =========================================================
    // GET
    // =========================================================

    public Role validateAndGet(
            String publicId) {

        if (!hasText(
                publicId
        )) {

            throw new BadRequestException(
                    "Role public ID is required."
            );
        }

        return roleRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                RoleMessages.ROLE_NOT_FOUND
                        )
                );
    }


    // =========================================================
    // ROLE CODE
    // =========================================================

    private void validateRoleCode(
            String roleCode) {

        if (!hasText(
                roleCode
        )) {

            throw new BadRequestException(
                    "Role code is required."
            );
        }

        if (roleCode.trim().length() > 50) {

            throw new BadRequestException(
                    "Role code must not exceed 50 characters."
            );
        }

        if (!roleCode
                .trim()
                .matches("^[A-Za-z0-9_-]+$")) {

            throw new BadRequestException(
                    "Role code may contain only letters, numbers, underscore and hyphen."
            );
        }
    }


    // =========================================================
    // ROLE NAME
    // =========================================================

    private void validateRoleName(
            String roleName) {

        if (!hasText(
                roleName
        )) {

            throw new BadRequestException(
                    "Role name is required."
            );
        }

        if (roleName.trim().length() > 100) {

            throw new BadRequestException(
                    "Role name must not exceed 100 characters."
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

        if (description.length() > 500) {

            throw new BadRequestException(
                    "Role description must not exceed 500 characters."
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

    private void validateSystemFlag(
            Integer isSystem) {

        if (isSystem == null) {
            return;
        }

        if (!isSystem.equals(0)
                && !isSystem.equals(1)) {

            throw new BadRequestException(
                    "isSystem must be either 0 or 1."
            );
        }
    }


    // =========================================================
    // DEFAULT FLAG
    // =========================================================

    private void validateDefaultFlag(
            Integer isDefault) {

        if (isDefault == null) {
            return;
        }

        if (!isDefault.equals(0)
                && !isDefault.equals(1)) {

            throw new BadRequestException(
                    "isDefault must be either 0 or 1."
            );
        }
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    private String normalize(
            String value) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }

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

        return first.equals(second);
    }
}