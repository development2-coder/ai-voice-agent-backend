package com.infinitio.aivoiceplatform.master.userpermission.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import com.infinitio.aivoiceplatform.master.permission.repository.PermissionRepository;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.rolepermission.repository.RolePermissionRepository;
import com.infinitio.aivoiceplatform.master.userpermission.dto.response.UserPermissionResponse;
import com.infinitio.aivoiceplatform.master.userpermission.entity.UserPermission;
import com.infinitio.aivoiceplatform.master.userpermission.repository.UserPermissionRepository;
import com.infinitio.aivoiceplatform.master.userpermission.service.UserPermissionService;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.ACTIVE;
import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.INACTIVE;
import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;

/**
 * Implements tenant-safe individual user permission assignment.
 *
 * An ADMIN can assign only permissions granted to the shared ADMIN role.
 * A USER receives only individually assigned permissions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPermissionServiceImpl implements UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public List<UserPermissionResponse> assignPermissions(
            String userPublicId,
            List<String> permissionPublicIds) {

        validateManagementAccess();

        if (userPublicId == null || userPublicId.isBlank()) {
            throw new IllegalArgumentException(
                    "User public ID is required."
            );
        }

        if (permissionPublicIds == null) {
            throw new IllegalArgumentException(
                    "Permission public IDs are required."
            );
        }

        User currentUser = currentUserService.getCurrentUser();
        User targetUser = findTargetUser(userPublicId.trim());

        validateTargetUser(currentUser, targetUser);

        /*
         * Resolve and validate every requested permission before changing
         * any existing assignment. This prevents partial updates.
         */
        Set<String> uniquePublicIds = new HashSet<>(permissionPublicIds);

        if (uniquePublicIds.size() != permissionPublicIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate permission public IDs are not allowed."
            );
        }

        List<Permission> requestedPermissions = new ArrayList<>();

        for (String permissionPublicId : uniquePublicIds) {
            if (permissionPublicId == null || permissionPublicId.isBlank()) {
                throw new IllegalArgumentException(
                        "Permission public ID cannot be blank."
                );
            }

            Permission permission = permissionRepository
                    .findByPublicIdAndIsDeleted(
                            permissionPublicId.trim(),
                            NOT_DELETED
                    )
                    .filter(item -> ACTIVE.equals(item.getIsActive()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active permission not found: " + permissionPublicId
                    ));

            validatePermissionAllowedForAdmin(permission);

            requestedPermissions.add(permission);
        }

        /*
         * Deactivate existing assignments not included in the new list.
         * Rows are retained for auditability and can be reactivated later.
         */
        Set<Long> requestedPermissionIds = new HashSet<>();
        for (Permission permission : requestedPermissions) {
            requestedPermissionIds.add(permission.getId());
        }

        List<UserPermission> existingAssignments =
                userPermissionRepository.findByUser_Id(targetUser.getId());

        for (UserPermission assignment : existingAssignments) {
            Long permissionId = assignment.getPermission().getId();

            if (requestedPermissionIds.contains(permissionId)) {
                continue;
            }

            if (ACTIVE.equals(assignment.getIsActive())
                    && NOT_DELETED.equals(assignment.getIsDeleted())) {
                assignment.setIsActive(INACTIVE);
                assignment.setUpdatedBy(currentUser.getId());
                userPermissionRepository.save(assignment);
            }
        }

        /*
         * Create missing assignments or reactivate previous assignments.
         */
        for (Permission permission : requestedPermissions) {
            UserPermission assignment = userPermissionRepository
                    .findByUser_IdAndPermission_Id(
                            targetUser.getId(),
                            permission.getId()
                    )
                    .orElse(null);

            if (assignment == null) {
                assignment = UserPermission.builder()
                        .user(targetUser)
                        .permission(permission)
                        .isActive(ACTIVE)
                        .isDeleted(NOT_DELETED)
                        .createdBy(currentUser.getId())
                        .build();
            } else {
                assignment.setIsActive(ACTIVE);
                assignment.setIsDeleted(NOT_DELETED);
                assignment.setUpdatedBy(currentUser.getId());
            }

            userPermissionRepository.save(assignment);
        }

        log.info(
                "User permissions updated. targetUserPublicId={}, actorUserId={}, permissionCount={}",
                targetUser.getPublicId(),
                currentUser.getId(),
                requestedPermissions.size()
        );

        return getActivePermissionResponses(targetUser.getId());
    }

    @Override
    public List<UserPermissionResponse> getUserPermissions(
            String userPublicId) {

        validateManagementAccess();

        User currentUser = currentUserService.getCurrentUser();
        User targetUser = findTargetUser(userPublicId);

        validateTargetUser(currentUser, targetUser);

        return getActivePermissionResponses(targetUser.getId());
    }

    private void validateManagementAccess() {
        if (!currentUserService.isAuthenticated()) {
            throw new ForbiddenException(
                    "Authentication is required."
            );
        }

        if (!currentUserService.isSuperAdmin()
                && !currentUserService.isAdmin()) {
            throw new ForbiddenException(
                    "Only ADMIN or SUPER_ADMIN can manage user permissions."
            );
        }
    }

    private User findTargetUser(String userPublicId) {
        if (userPublicId == null || userPublicId.isBlank()) {
            throw new IllegalArgumentException(
                    "User public ID is required."
            );
        }

        if (currentUserService.isSuperAdmin()) {
            return userRepository
                    .findByPublicIdAndIsDeleted(
                            userPublicId.trim(),
                            NOT_DELETED
                    )
                    .filter(user -> ACTIVE.equals(user.getIsActive()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found."
                    ));
        }

        Long tenantId = currentUserService.getCurrentTenantId();

        return userRepository
                .findByTenant_IdAndPublicIdAndIsDeleted(
                        tenantId,
                        userPublicId.trim(),
                        NOT_DELETED
                )
                .filter(user -> ACTIVE.equals(user.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found in your admin account."
                ));
    }

    private void validateTargetUser(
            User currentUser,
            User targetUser) {

        if (targetUser.getRole() == null
                || !RoleConstants.USER.equalsIgnoreCase(
                targetUser.getRole().getRoleCode())) {
            throw new ForbiddenException(
                    "Individual permissions can only be assigned to USER accounts."
            );
        }

        if (currentUserService.isSuperAdmin()) {
            return;
        }

        if (currentUser.getTenant() == null
                || targetUser.getTenant() == null
                || !currentUser.getTenant().getId()
                .equals(targetUser.getTenant().getId())) {
            throw new ForbiddenException(
                    "You cannot manage users belonging to another admin."
            );
        }
    }

    /**
     * Ensures that the requested permission is included in the
     * shared ADMIN role permissions configured by SUPER_ADMIN.
     */
    private void validatePermissionAllowedForAdmin(Permission permission) {
        boolean allowed = rolePermissionRepository
                .existsActivePermissionByRoleCodeAndPermissionCode(
                        RoleConstants.ADMIN,
                        permission.getPermissionCode()
                );

        if (!allowed) {
            throw new ForbiddenException(
                    "Permission is not available to ADMIN: "
                            + permission.getPermissionCode()
            );
        }
    }

    private List<UserPermissionResponse> getActivePermissionResponses(
            Long userId) {

        return userPermissionRepository
                .findActiveAssignmentsForUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserPermissionResponse toResponse(
            UserPermission assignment) {

        Permission permission = assignment.getPermission();

        String moduleName = permission.getModule() == null
                ? null
                : permission.getModule().getModuleName();

        return new UserPermissionResponse(
                permission.getPublicId(),
                permission.getPermissionCode(),
                permission.getPermissionName(),
                moduleName
        );
    }
}