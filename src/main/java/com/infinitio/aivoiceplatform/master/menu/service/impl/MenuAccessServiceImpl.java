
package com.infinitio.aivoiceplatform.master.menu.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.master.menu.dto.response.MenuResponse;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.mapper.MenuMapper;
import com.infinitio.aivoiceplatform.master.menu.repository.MenuRepository;
import com.infinitio.aivoiceplatform.master.menu.service.MenuAccessService;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.repository.RoleRepository;
import com.infinitio.aivoiceplatform.master.rolemenu.constant.RoleMenuConstants;
import com.infinitio.aivoiceplatform.master.rolemenu.entity.RoleMenu;
import com.infinitio.aivoiceplatform.master.rolemenu.repository.RoleMenuRepository;
import com.infinitio.aivoiceplatform.master.usermenu.constant.UserMenuConstants;
import com.infinitio.aivoiceplatform.master.usermenu.entity.UserMenu;
import com.infinitio.aivoiceplatform.master.usermenu.repository.UserMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Resolves menu access using shared role assignments and
 * user-specific menu assignments.
 *
 * <p>SUPER_ADMIN receives all active menus.</p>
 * <p>ADMIN receives menus assigned to the shared ADMIN role.</p>
 * <p>USER receives only menus assigned to that user.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuAccessServiceImpl implements MenuAccessService {

    private static final Integer ACTIVE = RoleMenuConstants.ACTIVE;
    private static final Integer NOT_DELETED = RoleMenuConstants.NOT_DELETED;
    private static final Integer VISIBLE = RoleMenuConstants.VISIBLE;

    private final CurrentUserService currentUserService;
    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final UserMenuRepository userMenuRepository;
    private final MenuMapper menuMapper;

    /**
     * Returns menus accessible to the authenticated user.
     *
     * @return accessible menus
     */
    @Override
    public List<MenuResponse> getAccessibleMenus() {
        String roleCode = currentUserService.getCurrentRoleCode();
        Long userId = currentUserService.getCurrentUserId();

        log.info(
                "Resolving accessible menus. userId={}, role={}",
                userId,
                roleCode
        );

        if (RoleConstants.SUPER_ADMIN.equalsIgnoreCase(roleCode)) {
            return getSuperAdminMenus();
        }

        if (RoleConstants.ADMIN.equalsIgnoreCase(roleCode)) {
            return getAdminMenus();
        }

        if (RoleConstants.USER.equalsIgnoreCase(roleCode)) {
            return getUserMenus();
        }

        log.warn("No menu access configuration found for role={}", roleCode);
        return List.of();
    }

    /**
     * Returns all active, non-deleted menus for SUPER_ADMIN.
     */
    private List<MenuResponse> getSuperAdminMenus() {
        return menuRepository
                .findByIsActiveAndIsDeletedOrderByDisplayOrderAsc(
                        ACTIVE,
                        NOT_DELETED
                )
                .stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    /**
     * Returns the shared menus assigned to the global ADMIN role.
     *
     * <p>No tenant-specific AdminMenu lookup is performed here.
     * Consequently, all client Admins with the ADMIN role receive
     * the same role-configured menu set.</p>
     */
    private List<MenuResponse> getAdminMenus() {
        Role adminRole = roleRepository
                .findByRoleCodeAndIsDeleted(
                        RoleConstants.ADMIN,
                        NOT_DELETED
                )
                .orElse(null);

        if (adminRole == null
                || !ACTIVE.equals(adminRole.getIsActive())) {
            log.warn("Active ADMIN role was not found.");
            return List.of();
        }

        List<RoleMenu> roleMenus =
                roleMenuRepository
                        .findByRoleIdAndIsActiveAndIsDeletedAndIsVisibleOrderByMenuDisplayOrderAsc(
                                adminRole.getId(),
                                ACTIVE,
                                NOT_DELETED,
                                VISIBLE
                        );

        return roleMenus.stream()
                .map(RoleMenu::getMenu)
                .filter(this::isMenuActive)
                .map(menuMapper::toResponse)
                .toList();
    }

    /**
     * Returns only the menus explicitly assigned to the current user.
     *
     * <p>The lookup uses the authenticated user's database ID,
     * so one user's assignments are not reused for another user.</p>
     */
    private List<MenuResponse> getUserMenus() {
        Long userId = currentUserService.getCurrentUserId();

        List<UserMenu> userMenus =
                userMenuRepository
                        .findByUserIdAndIsActiveAndIsDeletedAndIsVisibleOrderByMenuDisplayOrderAsc(
                                userId,
                                ACTIVE,
                                UserMenuConstants.NOT_DELETED,
                                UserMenuConstants.VISIBLE
                        );

        return userMenus.stream()
                .map(UserMenu::getMenu)
                .filter(this::isMenuActive)
                .map(menuMapper::toResponse)
                .toList();
    }

    /**
     * Checks whether a menu remains active and non-deleted.
     *
     * @param menu menu to check
     * @return true if the menu is active and non-deleted
     */
    private boolean isMenuActive(Menu menu) {
        return menu != null
                && ACTIVE.equals(menu.getIsActive())
                && NOT_DELETED.equals(menu.getIsDeleted());
    }
}
