package com.infinitio.aivoiceplatform.master.menu.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.menu.constant.MenuMessages;
import com.infinitio.aivoiceplatform.master.menu.dto.request.CreateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.request.UpdateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Menu Validator.
 *
 * Handles validation for Menu create, update
 * and lookup operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuValidator {

    private static final Integer NOT_DELETED = 0;

    private final MenuRepository menuRepository;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Validate Menu create request.
     *
     * @param request create request
     */
    public void validateForCreate(
            CreateMenuRequest request) {

        log.info(
                "Validating Menu create request."
        );

        /*
         * Request validation.
         */
        if (request == null) {

            throw new BadRequestException(
                    "Menu request is required."
            );
        }

        /*
         * Module validation.
         *
         * Actual PlatformModule existence is validated
         * by MenuServiceImpl using PlatformModuleValidator.
         */
        if (!hasText(
                request.getModulePublicId()
        )) {

            throw new BadRequestException(
                    "Module public ID is required."
            );
        }

        /*
         * Menu code validation.
         */
        if (!hasText(
                request.getMenuCode()
        )) {

            throw new BadRequestException(
                    "Menu code is required."
            );
        }

        /*
         * Menu name validation.
         */
        if (!hasText(
                request.getMenuName()
        )) {

            throw new BadRequestException(
                    "Menu name is required."
            );
        }

        /*
         * Display order validation.
         */
        validateDisplayOrder(
                request.getDisplayOrder()
        );

        /*
         * System flag validation.
         *
         * 0 = normal menu
         * 1 = system menu
         */
        validateFlag(
                request.getIsSystem(),
                "isSystem"
        );

        /*
         * Normalize values before checking duplicates.
         */
        String menuCode =
                normalize(
                        request.getMenuCode()
                );

        String menuName =
                normalize(
                        request.getMenuName()
                );

        /*
         * Check duplicate menu code.
         *
         * Only non-deleted records are considered.
         */
        if (menuRepository
                .existsByMenuCodeAndIsDeleted(
                        menuCode,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    MenuMessages.CODE_ALREADY_EXISTS
            );
        }

        /*
         * Check duplicate menu name.
         */
        if (menuRepository
                .existsByMenuNameAndIsDeleted(
                        menuName,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    MenuMessages.NAME_ALREADY_EXISTS
            );
        }

        /*
         * Validate parent menu if supplied.
         */
        validateParentMenuForCreate(
                request.getParentMenuPublicId()
        );

        log.info(
                "Menu create validation completed."
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Validate Menu update request.
     *
     * @param request update request
     */
    public void validateForUpdate(
            UpdateMenuRequest request) {

        log.info(
                "Validating Menu update request."
        );

        /*
         * Request validation.
         */
        if (request == null) {

            throw new BadRequestException(
                    "Menu update request is required."
            );
        }

        /*
         * Public ID validation.
         */
        if (!hasText(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "Menu public ID is required."
            );
        }

        /*
         * Module validation.
         */
        if (!hasText(
                request.getModulePublicId()
        )) {

            throw new BadRequestException(
                    "Module public ID is required."
            );
        }

        /*
         * Menu code validation.
         */
        if (!hasText(
                request.getMenuCode()
        )) {

            throw new BadRequestException(
                    "Menu code is required."
            );
        }

        /*
         * Menu name validation.
         */
        if (!hasText(
                request.getMenuName()
        )) {

            throw new BadRequestException(
                    "Menu name is required."
            );
        }

        /*
         * Display order validation.
         */
        validateDisplayOrder(
                request.getDisplayOrder()
        );

        /*
         * System flag validation.
         */
        validateFlag(
                request.getIsSystem(),
                "isSystem"
        );

        /*
         * Normalize public ID.
         */
        String publicId =
                request.getPublicId()
                        .trim();

        /*
         * Make sure the menu being updated exists.
         */
        Menu existingMenu =
                validateAndGet(
                        publicId
                );

        /*
         * Normalize code and name.
         */
        String menuCode =
                normalize(
                        request.getMenuCode()
                );

        String menuName =
                normalize(
                        request.getMenuName()
                );

        /*
         * Check duplicate menu code.
         *
         * Excludes the current menu.
         */
        if (menuRepository
                .existsByMenuCodeAndIsDeletedAndPublicIdNot(
                        menuCode,
                        NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    MenuMessages.CODE_ALREADY_EXISTS
            );
        }

        /*
         * Check duplicate menu name.
         *
         * Excludes the current menu.
         */
        if (menuRepository
                .existsByMenuNameAndIsDeletedAndPublicIdNot(
                        menuName,
                        NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    MenuMessages.NAME_ALREADY_EXISTS
            );
        }

        /*
         * Validate parent menu.
         */
        validateParentMenuForUpdate(
                request.getParentMenuPublicId(),
                existingMenu
        );

        log.info(
                "Menu update validation completed."
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Get a non-deleted Menu by public ID.
     *
     * This method is used by:
     *
     * - MenuServiceImpl
     * - RoleMenuValidator
     * - RoleMenuServiceImpl
     *
     * @param publicId menu public ID
     * @return Menu
     */
    public Menu validateAndGet(
            String publicId) {

        if (!hasText(
                publicId
        )) {

            throw new BadRequestException(
                    "Menu public ID is required."
            );
        }

        String normalizedPublicId =
                publicId.trim();

        return menuRepository
                .findByPublicIdAndIsDeleted(
                        normalizedPublicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MenuMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // PARENT MENU - CREATE
    // =========================================================

    /**
     * Validate parent menu during creation.
     *
     * @param parentMenuPublicId parent menu public ID
     */
    private void validateParentMenuForCreate(
            String parentMenuPublicId) {

        /*
         * Parent menu is optional.
         */
        if (!hasText(
                parentMenuPublicId
        )) {

            return;
        }

        String normalizedParentId =
                parentMenuPublicId.trim();

        /*
         * Parent must exist and must not be deleted.
         */
        menuRepository
                .findByPublicIdAndIsDeleted(
                        normalizedParentId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MenuMessages.PARENT_MENU_NOT_FOUND
                        )
                );
    }


    // =========================================================
    // PARENT MENU - UPDATE
    // =========================================================

    /**
     * Validate parent menu during update.
     *
     * Prevents:
     *
     * 1. Non-existing parent.
     * 2. Deleted parent.
     * 3. Menu being its own parent.
     *
     * @param parentMenuPublicId parent menu public ID
     * @param existingMenu menu being updated
     */
    private void validateParentMenuForUpdate(
            String parentMenuPublicId,
            Menu existingMenu) {

        /*
         * Parent is optional.
         *
         * If null/blank, the service will remove
         * the existing parent.
         */
        if (!hasText(
                parentMenuPublicId
        )) {

            return;
        }

        String normalizedParentId =
                parentMenuPublicId.trim();

        /*
         * Find parent.
         */
        Menu parentMenu =
                menuRepository
                        .findByPublicIdAndIsDeleted(
                                normalizedParentId,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MenuMessages.PARENT_MENU_NOT_FOUND
                                )
                        );

        /*
         * Prevent menu from becoming its own parent.
         */
        if (existingMenu != null
                && existingMenu.getId() != null
                && parentMenu.getId() != null
                && existingMenu
                .getId()
                .equals(
                        parentMenu.getId()
                )) {

            throw new BadRequestException(
                    "A menu cannot be its own parent."
            );
        }
    }


    // =========================================================
    // DISPLAY ORDER
    // =========================================================

    /**
     * Validate display order.
     *
     * Null is allowed because the service
     * provides default value 1 during create.
     *
     * @param displayOrder display order
     */
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
    // FLAG VALIDATION
    // =========================================================

    /**
     * Validate integer flag.
     *
     * Allowed values:
     *
     * 0 = false
     * 1 = true
     *
     * @param value flag value
     * @param fieldName field name
     */
    private void validateFlag(
            Integer value,
            String fieldName) {

        /*
         * Null is allowed because the service
         * supplies defaults.
         */
        if (value == null) {
            return;
        }

        if (!Integer.valueOf(0).equals(value)
                && !Integer.valueOf(1).equals(value)) {

            throw new BadRequestException(
                    fieldName
                            + " must be either 0 or 1."
            );
        }
    }


    // =========================================================
    // NORMALIZE
    // =========================================================

    /**
     * Trim string values.
     *
     * @param value input value
     * @return normalized value
     */
    private String normalize(
            String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }


    // =========================================================
    // HAS TEXT
    // =========================================================

    /**
     * Check whether a string contains text.
     *
     * @param value string value
     * @return true if non-blank
     */
    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}