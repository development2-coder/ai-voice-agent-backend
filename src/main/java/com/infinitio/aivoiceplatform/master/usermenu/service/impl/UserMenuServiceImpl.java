package com.infinitio.aivoiceplatform.master.usermenu.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.validator.MenuValidator;
import com.infinitio.aivoiceplatform.master.rolemenu.repository.RoleMenuRepository;
import com.infinitio.aivoiceplatform.master.usermenu.constant.UserMenuConstants;
import com.infinitio.aivoiceplatform.master.usermenu.constant.UserMenuMessages;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.CreateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.UpdateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.response.UserMenuResponse;
import com.infinitio.aivoiceplatform.master.usermenu.entity.UserMenu;
import com.infinitio.aivoiceplatform.master.usermenu.mapper.UserMenuMapper;
import com.infinitio.aivoiceplatform.master.usermenu.repository.UserMenuRepository;
import com.infinitio.aivoiceplatform.master.usermenu.service.UserMenuService;
import com.infinitio.aivoiceplatform.master.usermenu.validator.UserMenuValidator;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Menu Service Implementation.
 *
 * <p>
 * Handles user-level menu access while enforcing:
 * </p>
 *
 * <ul>
 *     <li>Common-library RBAC at controller level.</li>
 *     <li>SUPER_ADMIN global access.</li>
 *     <li>ADMIN tenant isolation.</li>
 *     <li>ADMIN RoleMenu boundary.</li>
 *     <li>USER management restrictions.</li>
 * </ul>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserMenuServiceImpl
        implements UserMenuService {

    private static final Integer ACTIVE =
            UserMenuConstants.ACTIVE;

    private static final Integer NOT_DELETED =
            UserMenuConstants.NOT_DELETED;

    private static final Integer VISIBLE =
            UserMenuConstants.VISIBLE;

    private final UserMenuRepository userMenuRepository;

    private final UserRepository userRepository;

    private final UserMenuMapper userMenuMapper;

    private final UserMenuValidator userMenuValidator;

    private final MenuValidator menuValidator;

    private final RoleMenuRepository roleMenuRepository;

    private final CurrentUserService currentUserService;

    private final RoleRepository roleRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public UserMenuResponse create(
            CreateUserMenuRequest request) {

        log.info(
                "Creating User Menu | userPublicId={} | menuPublicId={}",
                request != null
                        ? request.getUserPublicId()
                        : null,
                request != null
                        ? request.getMenuPublicId()
                        : null
        );

        userMenuValidator.validateForCreate(
                request
        );

        validateManagementAccess();

        User targetUser =
                getTargetUser(
                        request.getUserPublicId()
                );

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId()
                                .trim()
                );

        validateTargetUser(
                targetUser
        );

        validateMenu(
                menu
        );

        /*
         * CRITICAL:
         *
         * ADMIN can assign only menus which are already
         * available to that ADMIN through RoleMenu.
         */
        validateAdminMenuAccess(
                menu
        );

        UserMenu userMenu =
                userMenuMapper.toEntity(
                        request
                );

        userMenu.setUser(
                targetUser
        );

        userMenu.setMenu(
                menu
        );

        if (userMenu.getIsVisible() == null) {

            userMenu.setIsVisible(
                    VISIBLE
            );
        }

        userMenu.setIsActive(
                ACTIVE
        );

        userMenu.setIsDeleted(
                NOT_DELETED
        );

        userMenu.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        UserMenu saved =
                userMenuRepository.save(
                        userMenu
                );

        log.info(
                "User Menu created successfully | publicId={}",
                saved.getPublicId()
        );

        return userMenuMapper.toResponse(
                saved
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public UserMenuResponse update(
            UpdateUserMenuRequest request) {

        log.info(
                "Updating User Menu | publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        userMenuValidator.validateForUpdate(
                request
        );

        validateManagementAccess();

        UserMenu existing =
                userMenuValidator.validateAndGet(
                        request.getPublicId()
                                .trim()
                );

        /*
         * The existing UserMenu itself must also belong
         * to the current ADMIN's tenant.
         */
        validateExistingUserMenuAccess(
                existing
        );

        User targetUser =
                getTargetUser(
                        request.getUserPublicId()
                );

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId()
                                .trim()
                );

        validateTargetUser(
                targetUser
        );

        validateMenu(
                menu
        );

        validateAdminMenuAccess(
                menu
        );

        userMenuMapper.updateEntity(
                request,
                existing
        );

        existing.setUser(
                targetUser
        );

        existing.setMenu(
                menu
        );

        existing.setIsDeleted(
                NOT_DELETED
        );

        existing.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        UserMenu updated =
                userMenuRepository.save(
                        existing
                );

        log.info(
                "User Menu updated successfully | publicId={}",
                updated.getPublicId()
        );

        return userMenuMapper.toResponse(
                updated
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UserMenuResponse getByPublicId(
            String publicId) {

        validateManagementAccess();

        UserMenu userMenu =
                userMenuValidator.validateAndGet(
                        publicId
                );

        validateExistingUserMenuAccess(
                userMenu
        );

        return userMenuMapper.toResponse(
                userMenu
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserMenuResponse> getAll(
            int page,
            int size) {

        validateManagementAccess();

        if (page < 0) {

            throw new BadRequestException(
                    "Page number cannot be negative."
            );
        }

        if (size <= 0) {

            throw new BadRequestException(
                    "Page size must be greater than zero."
            );
        }

        Page<UserMenu> result;

        if (currentUserService.isSuperAdmin()) {

            result =
                    userMenuRepository.findByIsDeleted(
                            NOT_DELETED,
                            PageRequest.of(
                                    page,
                                    size
                            )
                    );

        } else {

            Long tenantId =
                    currentUserService.getCurrentTenantId();

            result =
                    userMenuRepository
                            .findByUser_Tenant_IdAndIsDeleted(
                                    tenantId,
                                    NOT_DELETED,
                                    PageRequest.of(
                                            page,
                                            size
                                    )
                            );
        }

        return PageResponse
                .<UserMenuResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        userMenuMapper::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        result.getNumber()
                )
                .pageSize(
                        result.getSize()
                )
                .totalPages(
                        result.getTotalPages()
                )
                .totalElements(
                        result.getTotalElements()
                )
                .first(
                        result.isFirst()
                )
                .last(
                        result.isLast()
                )
                .build();
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        validateManagementAccess();

        UserMenu userMenu =
                userMenuValidator.validateAndGet(
                        publicId
                );

        validateExistingUserMenuAccess(
                userMenu
        );

        userMenu.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        userMenuRepository.save(
                userMenu
        );

        log.info(
                "User Menu deleted successfully | publicId={}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        validateManagementAccess();

        UserMenu userMenu =
                userMenuValidator.validateAndGet(
                        publicId
                );

        validateExistingUserMenuAccess(
                userMenu
        );

        userMenu.activate(
                currentUserService.getCurrentUserId()
        );

        userMenuRepository.save(
                userMenu
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        validateManagementAccess();

        UserMenu userMenu =
                userMenuValidator.validateAndGet(
                        publicId
                );

        validateExistingUserMenuAccess(
                userMenu
        );

        userMenu.deactivate(
                currentUserService.getCurrentUserId()
        );

        userMenuRepository.save(
                userMenu
        );
    }


    // =========================================================
    // MANAGEMENT ACCESS
    // =========================================================

    /**
     * Validates that the current user can manage UserMenu.
     *
     * <p>
     * The controller additionally uses the common library
     * permission:
     *
     * hasPermission(null, 'USER_MENU_CREATE')
     *
     * This service-level check protects the business rule
     * independently of the controller.
     */
    private void validateManagementAccess() {

        if (!currentUserService.isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required."
            );
        }

        if (!currentUserService.isSuperAdmin()
                && !currentUserService.isAdmin()) {

            throw new ForbiddenException(
                    UserMenuMessages.ADMIN_ONLY
            );
        }
    }


    // =========================================================
    // TARGET USER
    // =========================================================

    private User getTargetUser(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    "User public ID is required."
            );
        }

        return userRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                UserMenuMessages.INVALID_USER
                        )
                );
    }


    // =========================================================
    // TARGET USER AUTHORIZATION
    // =========================================================

    /**
     * Enforces Step 13.
     *
     * SUPER_ADMIN:
     * Can manage appropriate users globally.
     *
     * ADMIN:
     * Can manage only USER accounts from the ADMIN's
     * own tenant.
     */
    private void validateTargetUser(
            User targetUser) {

        if (targetUser == null) {

            throw new ResourceNotFoundException(
                    UserMenuMessages.INVALID_USER
            );
        }

        if (!ACTIVE.equals(
                targetUser.getIsActive()
        )) {

            throw new BadRequestException(
                    "Target user is inactive."
            );
        }

        if (targetUser.getRole() == null
                || targetUser.getRole().getRoleCode() == null) {

            throw new BadRequestException(
                    "Target user role is unavailable."
            );
        }

        String roleCode =
                targetUser
                        .getRole()
                        .getRoleCode();

        /*
         * UserMenu is intentionally for individual USER
         * page access.
         *
         * ADMIN page access is controlled by RoleMenu.
         * SUPER_ADMIN page access is platform-level.
         */
        if (!currentUserService.isSuperAdmin()
                && !"USER".equalsIgnoreCase(
                roleCode
        )) {

            throw new ForbiddenException(
                    UserMenuMessages.USER_MENU_ONLY_FOR_USER
            );
        }

        /*
         * Even SUPER_ADMIN should not use UserMenu to
         * override ADMIN RoleMenu configuration.
         */
        if (currentUserService.isSuperAdmin()) {

            return;
        }

        Long currentTenantId =
                currentUserService.getCurrentTenantId();

        if (targetUser.getTenant() == null
                || targetUser.getTenant().getId() == null
                || !currentTenantId.equals(
                targetUser.getTenant().getId()
        )) {

            throw new ForbiddenException(
                    UserMenuMessages.USER_OUTSIDE_TENANT
            );
        }
    }


    // =========================================================
    // MENU VALIDATION
    // =========================================================

    private void validateMenu(
            Menu menu) {

        if (menu == null) {

            throw new ResourceNotFoundException(
                    UserMenuMessages.INVALID_MENU
            );
        }

        if (!ACTIVE.equals(
                menu.getIsActive()
        )) {

            throw new BadRequestException(
                    "Menu is inactive."
            );
        }

        if (menu.getIsDeleted() == null
                || !NOT_DELETED.equals(
                menu.getIsDeleted()
        )) {

            throw new ResourceNotFoundException(
                    UserMenuMessages.INVALID_MENU
            );
        }
    }


    // =========================================================
    // ADMIN MENU ACCESS
    // =========================================================

    /**
     * CRITICAL AUTHORIZATION RULE.
     *
     * <p>
     * An ADMIN cannot grant a USER a page that the ADMIN
     * itself does not have.
     * </p>
     *
     * <p>
     * The common library verifies the generic
     * USER_MENU_CREATE / UPDATE permission.
     *
     * This method performs the application-specific
     * RoleMenu boundary check.
     * </p>
     */
    /**
     * CRITICAL AUTHORIZATION RULE.
     *
     * <p>
     * An ADMIN cannot grant a USER a page that the ADMIN
     * itself does not have.
     * </p>
     *
     * <p>
     * SUPER_ADMIN has access to all menus.
     *
     * ADMIN access is determined by the application-specific
     * AdminMenu configuration of the ADMIN's tenant/client.
     *
     * USER_MENU_CREATE / UPDATE permission is checked by the
     * common-library RBAC layer.
     *
     * This method performs the additional application-specific
     * AdminMenu boundary check.
     * </p>
     *
     * @param menu menu that ADMIN wants to assign
     */

    /**
     * Validates whether the current administrator can assign
     * a menu to a user.
     *
     * <p>SUPER_ADMIN can manage menus globally.</p>
     *
     * <p>ADMIN can grant only menus assigned to the shared
     * ADMIN role by SUPER_ADMIN.</p>
     *
     * @param menu menu to assign
     */
    private void validateAdminMenuAccess(Menu menu) {

        if (currentUserService.isSuperAdmin()) {
            return;
        }

        if (!currentUserService.isAdmin()) {
            throw new ForbiddenException(
                    UserMenuMessages.ADMIN_ONLY
            );
        }

        if (menu == null
                || menu.getId() == null
                || !ACTIVE.equals(menu.getIsActive())
                || !NOT_DELETED.equals(menu.getIsDeleted())) {

            throw new BadRequestException(
                    UserMenuMessages.INVALID_MENU
            );
        }

        Role adminRole = roleRepository
                .findByRoleCodeAndIsDeleted(
                        RoleConstants.ADMIN,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shared ADMIN role was not found."
                        )
                );

        if (!ACTIVE.equals(adminRole.getIsActive())) {
            throw new ForbiddenException(
                    "The shared ADMIN role is inactive."
            );
        }

        boolean menuAvailableToAdmin =
                roleMenuRepository.existsActiveVisibleMenuForRole(
                        adminRole.getId(),
                        menu.getId(),
                        ACTIVE,
                        NOT_DELETED,
                        VISIBLE
                );

        if (!menuAvailableToAdmin) {
            log.warn(
                    "ADMIN attempted to assign a menu not enabled " +
                            "for the shared ADMIN role. userId={}, menuId={}",
                    currentUserService.getCurrentUserId(),
                    menu.getId()
            );

            throw new ForbiddenException(
                    UserMenuMessages.MENU_NOT_AVAILABLE
            );
        }
    }


    // =========================================================
    // EXISTING USER MENU AUTHORIZATION
    // =========================================================

    /**
     * Prevents an ADMIN from modifying an existing UserMenu
     * belonging to another tenant.
     */
    private void validateExistingUserMenuAccess(
            UserMenu userMenu) {

        if (currentUserService.isSuperAdmin()) {

            return;
        }

        if (!currentUserService.isAdmin()) {

            throw new ForbiddenException(
                    UserMenuMessages.ADMIN_ONLY
            );
        }

        if (userMenu.getUser() == null
                || userMenu.getUser().getTenant() == null
                || userMenu.getUser()
                .getTenant()
                .getId() == null) {

            throw new ForbiddenException(
                    UserMenuMessages.USER_OUTSIDE_TENANT
            );
        }

        Long currentTenantId =
                currentUserService.getCurrentTenantId();

        Long targetTenantId =
                userMenu.getUser()
                        .getTenant()
                        .getId();

        if (!currentTenantId.equals(
                targetTenantId
        )) {

            throw new ForbiddenException(
                    UserMenuMessages.USER_OUTSIDE_TENANT
            );
        }

        /*
         * Also ensure the existing target is still a USER.
         */
        if (userMenu.getUser().getRole() == null
                || !"USER".equalsIgnoreCase(
                userMenu.getUser()
                        .getRole()
                        .getRoleCode()
        )) {

            throw new ForbiddenException(
                    UserMenuMessages.USER_MENU_ONLY_FOR_USER
            );
        }
    }
}