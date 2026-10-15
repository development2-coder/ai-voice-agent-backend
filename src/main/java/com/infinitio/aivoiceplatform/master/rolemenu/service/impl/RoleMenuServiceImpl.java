package com.infinitio.aivoiceplatform.master.rolemenu.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.validator.MenuValidator;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.validator.RoleValidator;
import com.infinitio.aivoiceplatform.master.rolemenu.constant.RoleMenuConstants;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.CreateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.UpdateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.response.RoleMenuResponse;
import com.infinitio.aivoiceplatform.master.rolemenu.entity.RoleMenu;
import com.infinitio.aivoiceplatform.master.rolemenu.mapper.RoleMenuMapper;
import com.infinitio.aivoiceplatform.master.rolemenu.repository.RoleMenuRepository;
import com.infinitio.aivoiceplatform.master.rolemenu.service.RoleMenuService;
import com.infinitio.aivoiceplatform.master.rolemenu.validator.RoleMenuValidator;
import lombok.RequiredArgsConstructor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
/**
 * Role Menu Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleMenuServiceImpl
        implements RoleMenuService {

    private static final Integer ACTIVE =
            RoleMenuConstants.ACTIVE;

    private static final Integer NOT_DELETED =
            RoleMenuConstants.NOT_DELETED;

    private final RoleMenuRepository roleMenuRepository;

    private final RoleMenuMapper roleMenuMapper;

    private final RoleMenuValidator roleMenuValidator;

    private final RoleValidator roleValidator;

    private final MenuValidator menuValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public RoleMenuResponse create(
            CreateRoleMenuRequest request) {

        log.info(
                "Creating Role Menu | rolePublicId={} | menuPublicId={}",
                request != null
                        ? request.getRolePublicId()
                        : null,
                request != null
                        ? request.getMenuPublicId()
                        : null
        );

        validateSuperAdminAccess();

        /*
         * Validate request and duplicate mapping.
         */
        roleMenuValidator.validateForCreate(
                request
        );

        /*
         * Resolve relationships once.
         */
        Role role =
                roleValidator.validateAndGet(
                        request.getRolePublicId().trim()
                );

        validateRoleAssignment(role);

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId().trim()
                );

        /*
         * Map request to entity.
         */
        RoleMenu roleMenu =
                roleMenuMapper.toEntity(
                        request
                );

        roleMenu.setRole(
                role
        );

        roleMenu.setMenu(
                menu
        );

        /*
         * Default visibility.
         */
        if (roleMenu.getIsVisible() == null) {

            roleMenu.setIsVisible(
                    RoleMenuConstants.VISIBLE
            );
        }

        /*
         * New mapping is active.
         */
        roleMenu.setIsActive(
                ACTIVE
        );

        roleMenu.setIsDeleted(
                NOT_DELETED
        );

        /*
         * Audit.
         */
        roleMenu.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        RoleMenu savedRoleMenu =
                roleMenuRepository.save(
                        roleMenu
                );

        log.info(
                "Role Menu created successfully | publicId={}",
                savedRoleMenu.getPublicId()
        );

        return roleMenuMapper.toResponse(
                savedRoleMenu
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public RoleMenuResponse update(
            UpdateRoleMenuRequest request) {

        log.info(
                "Updating Role Menu | publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        validateSuperAdminAccess();

        /*
         * Validate update.
         */
        roleMenuValidator.validateForUpdate(
                request
        );

        /*
         * Get existing mapping.
         */
        RoleMenu roleMenu =
                roleMenuValidator.validateAndGet(
                        request.getPublicId().trim()
                );

        /*
         * Resolve relationships.
         */
        Role role =
                roleValidator.validateAndGet(
                        request.getRolePublicId().trim()
                );

        validateRoleAssignment(role);

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId().trim()
                );

        /*
         * Map simple fields.
         *
         * Role/Menu/audit fields are ignored by mapper.
         */
        roleMenuMapper.updateEntity(
                request,
                roleMenu
        );

        roleMenu.setRole(
                role
        );

        roleMenu.setMenu(
                menu
        );

        /*
         * Preserve lifecycle state.
         */
        roleMenu.setIsDeleted(
                NOT_DELETED
        );

        /*
         * Audit.
         */
        roleMenu.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        RoleMenu updatedRoleMenu =
                roleMenuRepository.save(
                        roleMenu
                );

        log.info(
                "Role Menu updated successfully | publicId={}",
                updatedRoleMenu.getPublicId()
        );

        return roleMenuMapper.toResponse(
                updatedRoleMenu
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RoleMenuResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Role Menu | publicId={}",
                publicId
        );

        RoleMenu roleMenu =
                roleMenuValidator.validateAndGet(
                        publicId
                );

        return roleMenuMapper.toResponse(
                roleMenu
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RoleMenuResponse> getAll() {

        log.info("Fetching all Role Menus.");

        return roleMenuRepository
                .findByIsDeletedOrderByRole_RoleNameAscMenu_DisplayOrderAsc(
                        NOT_DELETED
                )
                .stream()
                .map(roleMenuMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleMenuResponse> getByRolePublicId(
            String rolePublicId) {

        log.info(
                "Fetching menus assigned to role. rolePublicId={}",
                rolePublicId
        );

        Role role = roleValidator.validateAndGet(
                rolePublicId
        );

        validateRoleAssignment(role);

        return roleMenuRepository
                .findByRole_IdAndIsDeletedOrderByMenu_DisplayOrderAsc(
                        role.getId(),
                        NOT_DELETED
                )
                .stream()
                .map(roleMenuMapper::toResponse)
                .toList();
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Role Menu | publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        RoleMenu roleMenu =
                roleMenuValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        roleMenu.markAsDeleted(
                currentUserId
        );

        roleMenuRepository.save(
                roleMenu
        );

        log.info(
                "Role Menu deleted successfully | publicId={}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Role Menu | publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        RoleMenu roleMenu =
                roleMenuValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        roleMenu.activate(
                currentUserId
        );

        roleMenuRepository.save(
                roleMenu
        );

        log.info(
                "Role Menu activated successfully | publicId={}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Role Menu | publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        RoleMenu roleMenu =
                roleMenuValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        roleMenu.deactivate(
                currentUserId
        );

        roleMenuRepository.save(
                roleMenu
        );

        log.info(
                "Role Menu deactivated successfully | publicId={}",
                publicId
        );
    }

    /**
     * Validates that the current user is SUPER_ADMIN.
     *
     * <p>
     * Role-level menu assignments are platform-level
     * security configuration and can only be managed
     * by SUPER_ADMIN.
     * </p>
     */
    private void validateSuperAdminAccess() {

        if (!currentUserService.isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required to manage role menus."
            );
        }

        if (!currentUserService.isSuperAdmin()) {

            throw new ForbiddenException(
                    "Only SUPER_ADMIN can manage role menus."
            );
        }
    }

    /**
     * Validates that the selected role is allowed to receive
     * role-level menu assignments.
     *
     * <p>
     * RoleMenu defines the menus available to an ADMIN.
     * SUPER_ADMIN has full platform access and USER menu access
     * is managed separately through UserMenu.
     * </p>
     *
     * @param role role receiving the menu assignment
     */

    /**
     * Validates that the selected role can receive shared
     * role-menu assignments.
     *
     * <p>SUPER_ADMIN configures the menus available to every
     * client Admin through the shared ADMIN role.</p>
     *
     * <p>USER menu assignments remain user-specific and are
     * managed separately through UserMenu.</p>
     *
     * @param role role receiving the menu assignment
     */
    private void validateRoleAssignment(Role role) {

        if (role == null) {
            throw new BadRequestException(
                    "Role is required."
            );
        }

        if (role.getRoleCode() == null
                || !RoleConstants.ADMIN.equalsIgnoreCase(
                role.getRoleCode().trim()
        )) {

            throw new ForbiddenException(
                    "Shared role-menu assignments can only be configured for ADMIN."
            );
        }
    }

}