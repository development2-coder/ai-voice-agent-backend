package com.infinitio.aivoiceplatform.master.menu.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.master.menu.dto.request.CreateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.request.UpdateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.response.MenuResponse;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.mapper.MenuMapper;
import com.infinitio.aivoiceplatform.master.menu.repository.MenuRepository;
import com.infinitio.aivoiceplatform.master.menu.service.MenuService;
import com.infinitio.aivoiceplatform.master.menu.validator.MenuValidator;
import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
import com.infinitio.aivoiceplatform.master.platformmodule.validator.PlatformModuleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Menu Service Implementation.
 *
 * Handles Menu business operations and platform-level
 * authorization.
 *
 * Menu management is restricted to SUPER_ADMIN because
 * menus are platform-level configuration.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuServiceImpl
        implements MenuService {

    private static final Integer ACTIVE = 1;
    private static final Integer NOT_DELETED = 0;

    private final MenuRepository menuRepository;

    private final MenuMapper menuMapper;

    private final MenuValidator menuValidator;

    private final PlatformModuleValidator platformModuleValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // AUTHORIZATION
    // =========================================================

    /**
     * Validates that the current user is a Super Admin.
     *
     * Menu configuration is platform-level configuration
     * and therefore cannot be managed by ADMIN or USER.
     */
    private void validateSuperAdminAccess() {

        if (!currentUserService.isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required to manage menus."
            );
        }

        if (!currentUserService.isSuperAdmin()) {

            throw new ForbiddenException(
                    "Only Super Admin can manage menus."
            );
        }
    }


    // =========================================================
    // CREATE MENU
    // =========================================================

    @Override
    public MenuResponse create(
            CreateMenuRequest request) {

        validateSuperAdminAccess();

        log.info(
                "Creating Menu. menuCode={}",
                request != null
                        ? request.getMenuCode()
                        : null
        );

        /*
         * Validate request.
         */
        menuValidator.validateForCreate(
                request
        );

        /*
         * Validate and get Platform Module.
         */
        PlatformModule module =
                platformModuleValidator.validateAndGet(
                        request.getModulePublicId().trim()
                );

        /*
         * Map request to entity.
         */
        Menu menu =
                menuMapper.toEntity(
                        request
                );

        /*
         * Set module relationship.
         */
        menu.setModule(
                module
        );

        /*
         * Set parent menu when supplied.
         */
        if (request.getParentMenuPublicId() != null
                && !request
                .getParentMenuPublicId()
                .isBlank()) {

            Menu parentMenu =
                    menuValidator.validateAndGet(
                            request
                                    .getParentMenuPublicId()
                                    .trim()
                    );

            menu.setParentMenu(
                    parentMenu
            );
        }

        /*
         * Default display order.
         */
        if (menu.getDisplayOrder() == null) {

            menu.setDisplayOrder(
                    1
            );
        }

        /*
         * Default system flag.
         */
        if (menu.getIsSystem() == null) {

            menu.setIsSystem(
                    0
            );
        }


        /*
         * Explicit lifecycle defaults.
         */
        menu.setIsActive(
                ACTIVE
        );

        menu.setIsDeleted(
                NOT_DELETED
        );

        /*
         * Set audit information.
         */
        menu.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        /*
         * Save menu.
         */
        Menu savedMenu =
                menuRepository.save(
                        menu
                );


        log.info(
                "Menu created successfully. publicId={}",
                savedMenu.getPublicId()
        );

        return menuMapper.toResponse(
                savedMenu
        );
    }


    // =========================================================
    // UPDATE MENU
    // =========================================================

    @Override
    public MenuResponse update(
            UpdateMenuRequest request) {

        validateSuperAdminAccess();

        log.info(
                "Updating Menu. publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        /*
         * Validate update request and uniqueness.
         */
        menuValidator.validateForUpdate(
                request
        );

        /*
         * Get existing non-deleted menu.
         */
        Menu menu =
                menuValidator.validateAndGet(
                        request
                                .getPublicId()
                                .trim()
                );

        /*
         * Validate and get Platform Module.
         */
        PlatformModule module =
                platformModuleValidator.validateAndGet(
                        request
                                .getModulePublicId()
                                .trim()
                );

        /*
         * Map editable fields.
         */
        menuMapper.updateEntity(
                request,
                menu
        );

        /*
         * Set module relationship.
         */
        menu.setModule(
                module
        );

        /*
         * Update parent menu.
         *
         * Blank/null means remove parent.
         */
        if (request.getParentMenuPublicId() != null
                && !request
                .getParentMenuPublicId()
                .isBlank()) {

            Menu parentMenu =
                    menuValidator.validateAndGet(
                            request
                                    .getParentMenuPublicId()
                                    .trim()
                    );

            /*
             * Prevent self-parenting.
             */
            if (menu.getId() != null
                    && parentMenu.getId() != null
                    && menu.getId()
                    .equals(
                            parentMenu.getId()
                    )) {

                throw new BadRequestException(
                        "A menu cannot be its own parent."
                );
            }

            menu.setParentMenu(
                    parentMenu
            );

        } else {

            /*
             * Explicitly remove parent.
             */
            menu.setParentMenu(
                    null
            );
        }

        /*
         * Preserve lifecycle state.
         */
        menu.setIsDeleted(
                NOT_DELETED
        );

        /*
         * Save updated menu.
         */
        Menu updatedMenu =
                menuRepository.save(
                        menu
                );

        log.info(
                "Menu updated successfully. publicId={}",
                updatedMenu.getPublicId()
        );

        return menuMapper.toResponse(
                updatedMenu
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Menu. publicId={}",
                publicId
        );

        Menu menu =
                menuValidator.validateAndGet(
                        publicId
                );

        return menuMapper.toResponse(
                menu
        );
    }


    // =========================================================
    // GET ALL MENUS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getAll() {

        log.info("Fetching all Menus.");

        return menuRepository
                .findByIsDeletedOrderByDisplayOrderAsc(
                        NOT_DELETED
                )
                .stream()
                .map(menuMapper::toResponse)
                .toList();
    }


    // =========================================================
    // DELETE MENU
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        validateSuperAdminAccess();

        log.info(
                "Deleting Menu. publicId={}",
                publicId
        );

        /*
         * Get existing non-deleted menu.
         */
        Menu menu =
                menuValidator.validateAndGet(
                        publicId
                );

        /*
         * Soft delete.
         */
        menu.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        menuRepository.save(
                menu
        );

        log.info(
                "Menu deleted successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE MENU
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        validateSuperAdminAccess();

        log.info(
                "Activating Menu. publicId={}",
                publicId
        );

        Menu menu =
                menuValidator.validateAndGet(
                        publicId
                );

        menu.activate(
                currentUserService.getCurrentUserId()
        );

        menuRepository.save(
                menu
        );

        log.info(
                "Menu activated successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE MENU
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        validateSuperAdminAccess();

        log.info(
                "Deactivating Menu. publicId={}",
                publicId
        );

        Menu menu =
                menuValidator.validateAndGet(
                        publicId
                );

        menu.deactivate(
                currentUserService.getCurrentUserId()
        );

        menuRepository.save(
                menu
        );

        log.info(
                "Menu deactivated successfully. publicId={}",
                publicId
        );
    }
}