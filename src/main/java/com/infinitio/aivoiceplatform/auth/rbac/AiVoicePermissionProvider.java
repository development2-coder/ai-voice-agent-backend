package com.infinitio.aivoiceplatform.auth.rbac;

import com.infi.library.rbac.provider.PermissionProvider;
import com.infinitio.aivoiceplatform.auth.security.UserPrincipal;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.rolepermission.repository.RolePermissionRepository;
import com.infinitio.aivoiceplatform.master.userpermission.repository.UserPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Provides application permissions to the shared security library.
 *
 * SUPER_ADMIN has global access.
 * ADMIN uses permissions configured on the shared ADMIN role.
 * USER must have an individual assignment and the permission must
 * also remain enabled for the shared ADMIN role.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiVoicePermissionProvider implements PermissionProvider {

    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Override
    public boolean hasPermission(String permissionCode) {

        if (permissionCode == null || permissionCode.isBlank()) {
            return false;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        if (userPrincipal.getUser() == null
                || userPrincipal.getUser().getRole() == null
                || userPrincipal.getUser().getRole().getRoleCode() == null) {
            return false;
        }

        String roleCode = userPrincipal.getUser()
                .getRole()
                .getRoleCode();

        String normalizedPermissionCode = permissionCode.trim();

        if (RoleConstants.SUPER_ADMIN.equalsIgnoreCase(roleCode)) {
            return true;
        }

        /*
         * ADMIN can use only permissions assigned to the shared ADMIN role.
         */
        if (RoleConstants.ADMIN.equalsIgnoreCase(roleCode)) {
            return rolePermissionRepository
                    .existsActivePermissionByRoleCodeAndPermissionCode(
                            RoleConstants.ADMIN,
                            normalizedPermissionCode
                    );
        }

        /*
         * USER must have an individual assignment AND the permission must
         * still be allowed for ADMIN. Removing a permission from the shared
         * ADMIN role therefore revokes it for all users too.
         */
        if (RoleConstants.USER.equalsIgnoreCase(roleCode)) {

            Long userId = userPrincipal.getUser().getId();

            if (userId == null) {
                return false;
            }

            boolean allowedForAdmin = rolePermissionRepository
                    .existsActivePermissionByRoleCodeAndPermissionCode(
                            RoleConstants.ADMIN,
                            normalizedPermissionCode
                    );

            if (!allowedForAdmin) {
                return false;
            }

            boolean individuallyAssigned = userPermissionRepository
                    .existsActivePermissionForUser(
                            userId,
                            normalizedPermissionCode
                    );

            log.debug(
                    "User permission evaluated. userId={}, permissionCode={}, granted={}",
                    userId,
                    normalizedPermissionCode,
                    individuallyAssigned
            );

            return individuallyAssigned;
        }

        return false;
    }
}