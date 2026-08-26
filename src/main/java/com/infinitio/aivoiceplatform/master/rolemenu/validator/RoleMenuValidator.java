package com.infinitio.aivoiceplatform.master.rolemenu.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.validator.MenuValidator;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.validator.RoleValidator;
import com.infinitio.aivoiceplatform.master.rolemenu.constant.RoleMenuConstants;
import com.infinitio.aivoiceplatform.master.rolemenu.constant.RoleMenuMessages;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.CreateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.UpdateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.entity.RoleMenu;
import com.infinitio.aivoiceplatform.master.rolemenu.repository.RoleMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for Role Menu business validations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleMenuValidator {

    private static final Integer NOT_DELETED =
            RoleMenuConstants.NOT_DELETED;

    private final RoleMenuRepository roleMenuRepository;

    private final RoleValidator roleValidator;

    private final MenuValidator menuValidator;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateRoleMenuRequest request) {

        log.info(
                "Validating Role Menu create request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Role Menu request is required."
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
                request.getMenuPublicId()
        )) {

            throw new BadRequestException(
                    "Menu public ID is required."
            );
        }

        validateVisibility(
                request.getIsVisible()
        );

        /*
         * Resolve Role.
         */
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
                    RoleMenuMessages.INVALID_ROLE
            );
        }

        /*
         * Resolve Menu.
         */
        Menu menu;

        try {

            menu =
                    menuValidator.validateAndGet(
                            request
                                    .getMenuPublicId()
                                    .trim()
                    );

        } catch (ResourceNotFoundException exception) {

            throw new ResourceNotFoundException(
                    RoleMenuMessages.INVALID_MENU
            );
        }

        /*
         * Check duplicate mapping.
         */
        if (roleMenuRepository
                .existsByRoleIdAndMenuIdAndIsDeleted(
                        role.getId(),
                        menu.getId(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    RoleMenuMessages.ALREADY_EXISTS
            );
        }

        log.info(
                "Role Menu create validation completed."
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateRoleMenuRequest request) {

        log.info(
                "Validating Role Menu update request."
        );

        if (request == null) {

            throw new BadRequestException(
                    "Role Menu request is required."
            );
        }

        if (!hasText(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "Role Menu public ID is required."
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
                request.getMenuPublicId()
        )) {

            throw new BadRequestException(
                    "Menu public ID is required."
            );
        }

        validateVisibility(
                request.getIsVisible()
        );

        /*
         * Get current mapping.
         */
        RoleMenu existing =
                validateAndGet(
                        request
                                .getPublicId()
                                .trim()
                );

        /*
         * Resolve Role.
         */
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
                    RoleMenuMessages.INVALID_ROLE
            );
        }

        /*
         * Resolve Menu.
         */
        Menu menu;

        try {

            menu =
                    menuValidator.validateAndGet(
                            request
                                    .getMenuPublicId()
                                    .trim()
                    );

        } catch (ResourceNotFoundException exception) {

            throw new ResourceNotFoundException(
                    RoleMenuMessages.INVALID_MENU
            );
        }

        /*
         * Check duplicate mapping while excluding
         * the current RoleMenu record.
         */
        if (roleMenuRepository
                .existsByRoleIdAndMenuIdAndIsDeletedAndIdNot(
                        role.getId(),
                        menu.getId(),
                        NOT_DELETED,
                        existing.getId()
                )) {

            throw new ConflictException(
                    RoleMenuMessages.ALREADY_EXISTS
            );
        }

        log.info(
                "Role Menu update validation completed."
        );
    }


    // =========================================================
    // GET
    // =========================================================

    public RoleMenu validateAndGet(
            String publicId) {

        if (!hasText(
                publicId
        )) {

            throw new BadRequestException(
                    "Role Menu public ID is required."
            );
        }

        return roleMenuRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                RoleMenuMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // VISIBILITY
    // =========================================================

    private void validateVisibility(
            Integer isVisible) {

        if (isVisible == null) {
            return;
        }

        if (!Integer.valueOf(
                RoleMenuConstants.VISIBLE
        ).equals(isVisible)
                && !Integer.valueOf(
                RoleMenuConstants.HIDDEN
        ).equals(isVisible)) {

            throw new BadRequestException(
                    RoleMenuMessages
                            .INVALID_VISIBILITY
            );
        }
    }


    // =========================================================
    // STRING
    // =========================================================

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}