package com.infinitio.aivoiceplatform.master.rolepermission.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import com.infinitio.aivoiceplatform.master.permission.validator.PermissionValidator;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.validator.RoleValidator;
import com.infinitio.aivoiceplatform.master.rolepermission.constant.RolePermissionConstants;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.CreateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.UpdateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.response.RolePermissionResponse;
import com.infinitio.aivoiceplatform.master.rolepermission.entity.RolePermission;
import com.infinitio.aivoiceplatform.master.rolepermission.mapper.RolePermissionMapper;
import com.infinitio.aivoiceplatform.master.rolepermission.repository.RolePermissionRepository;
import com.infinitio.aivoiceplatform.master.rolepermission.service.RolePermissionService;
import com.infinitio.aivoiceplatform.master.rolepermission.validator.RolePermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.SaveRolePermissionsRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Role Permission Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionServiceImpl
        implements RolePermissionService {

    private static final Integer ACTIVE =
            RolePermissionConstants.ACTIVE;

    private static final Integer NOT_DELETED =
            RolePermissionConstants.NOT_DELETED;

    private final RolePermissionRepository rolePermissionRepository;

    private final RolePermissionMapper rolePermissionMapper;

    private final RolePermissionValidator rolePermissionValidator;

    private final RoleValidator roleValidator;

    private final PermissionValidator permissionValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public RolePermissionResponse create(
            CreateRolePermissionRequest request) {

        log.info(
                "Creating Role Permission | rolePublicId={} | permissionPublicId={}",
                request != null
                        ? request.getRolePublicId()
                        : null,
                request != null
                        ? request.getPermissionPublicId()
                        : null
        );

        validateSuperAdminAccess();

        rolePermissionValidator.validateForCreate(
                request
        );

        Role role =
                roleValidator.validateAndGet(
                        request
                                .getRolePublicId()
                                .trim()
                );

        validateRoleAssignment(role);

        Permission permission =
                permissionValidator.validateAndGet(
                        request
                                .getPermissionPublicId()
                                .trim()
                );

        RolePermission entity =
                rolePermissionMapper.toEntity(
                        request
                );

        entity.setRole(
                role
        );

        entity.setPermission(
                permission
        );

        entity.setIsActive(
                ACTIVE
        );

        entity.setIsDeleted(
                NOT_DELETED
        );

        entity.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        RolePermission saved =
                rolePermissionRepository.save(
                        entity
                );

        log.info(
                "Role Permission created successfully | publicId={}",
                saved.getPublicId()
        );

        return rolePermissionMapper.toResponse(
                saved
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public RolePermissionResponse update(
            UpdateRolePermissionRequest request) {

        log.info(
                "Updating Role Permission | publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        validateSuperAdminAccess();

        rolePermissionValidator.validateForUpdate(
                request
        );

        RolePermission entity =
                rolePermissionValidator.validateAndGet(
                        request
                                .getPublicId()
                                .trim()
                );

        Role role =
                roleValidator.validateAndGet(
                        request
                                .getRolePublicId()
                                .trim()
                );

        validateRoleAssignment(role);

        Permission permission =
                permissionValidator.validateAndGet(
                        request
                                .getPermissionPublicId()
                                .trim()
                );

        rolePermissionMapper.updateEntity(
                request,
                entity
        );

        entity.setRole(
                role
        );

        entity.setPermission(
                permission
        );

        entity.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        RolePermission updated =
                rolePermissionRepository.save(
                        entity
                );

        log.info(
                "Role Permission updated successfully | publicId={}",
                updated.getPublicId()
        );

        return rolePermissionMapper.toResponse(
                updated
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RolePermissionResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Role Permission | publicId={}",
                publicId
        );

        RolePermission entity =
                rolePermissionValidator.validateAndGet(
                        publicId
                );

        return rolePermissionMapper.toResponse(
                entity
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getAll() {

        log.info("Fetching all Role Permissions.");

        return rolePermissionRepository
                .findByIsDeletedOrderByRole_RoleNameAscPermission_PermissionNameAsc(
                        NOT_DELETED
                )
                .stream()
                .map(rolePermissionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getByRolePublicId(
            String rolePublicId) {

        log.info(
                "Fetching permissions assigned to role. rolePublicId={}",
                rolePublicId
        );

        Role role = roleValidator.validateAndGet(
                rolePublicId
        );

        validateRoleAssignment(role);

        return rolePermissionRepository
                .findByRole_IdAndIsActiveAndIsDeletedOrderByPermission_PermissionNameAsc(
                        role.getId(),
                        ACTIVE,
                        NOT_DELETED
                )
                .stream()
                .map(rolePermissionMapper::toResponse)
                .toList();
    }


    @Override
    public List<RolePermissionResponse> saveForRole(
            String rolePublicId,
            SaveRolePermissionsRequest request) {

        log.info(
                "Saving role permissions. rolePublicId={}",
                rolePublicId
        );

        validateSuperAdminAccess();

        if (request == null || request.getPermissionPublicIds() == null) {
            throw new BadRequestException(
                    "Permission list is required."
            );
        }

        Role role = roleValidator.validateAndGet(
                rolePublicId
        );

        validateRoleAssignment(role);

        Long currentUserId =
                currentUserService.getCurrentUserId();

        /*
         * Validate and deduplicate requested permission IDs.
         * Duplicate IDs are rejected rather than silently accepted.
         */
        Set<String> requestedPublicIds = new HashSet<>();

        Map<Long, Permission> selectedPermissions = new HashMap<>();

        for (String permissionPublicId : request.getPermissionPublicIds()) {

            if (permissionPublicId == null
                    || permissionPublicId.isBlank()) {

                throw new BadRequestException(
                        "Permission public ID cannot be blank."
                );
            }

            String normalizedPublicId = permissionPublicId.trim();

            if (!requestedPublicIds.add(normalizedPublicId)) {
                throw new BadRequestException(
                        "Duplicate permission public ID: "
                                + normalizedPublicId
                );
            }

            Permission permission =
                    permissionValidator.validateAndGet(
                            normalizedPublicId
                    );

            selectedPermissions.put(
                    permission.getId(),
                    permission
            );
        }

        /*
         * Load existing mappings, including soft-deleted records.
         * Reusing an existing record avoids violating the unique
         * role_id + permission_id database constraint.
         */
        List<RolePermission> existingMappings =
                rolePermissionRepository
                        .findByRole_IdOrderByPermission_PermissionNameAsc(
                                role.getId()
                        );

        Map<Long, RolePermission> existingByPermissionId =
                new HashMap<>();

        for (RolePermission mapping : existingMappings) {
            existingByPermissionId.put(
                    mapping.getPermission().getId(),
                    mapping
            );
        }

        List<RolePermission> mappingsToSave =
                new ArrayList<>();

        /*
         * Activate selected existing mappings and soft-delete
         * assignments that are no longer selected.
         */
        for (RolePermission mapping : existingMappings) {

            Long permissionId =
                    mapping.getPermission().getId();

            boolean selected =
                    selectedPermissions.containsKey(permissionId);

            if (selected) {

                boolean needsUpdate =
                        !Integer.valueOf(NOT_DELETED)
                                .equals(mapping.getIsDeleted())
                                || !Integer.valueOf(ACTIVE)
                                .equals(mapping.getIsActive());

                if (needsUpdate) {

                    mapping.setIsDeleted(NOT_DELETED);
                    mapping.setIsActive(ACTIVE);
                    mapping.setDeletedAt(null);
                    mapping.setUpdatedBy(currentUserId);

                    mappingsToSave.add(mapping);
                }

            } else if (Integer.valueOf(NOT_DELETED)
                    .equals(mapping.getIsDeleted())) {

                mapping.markAsDeleted(currentUserId);
                mappingsToSave.add(mapping);
            }
        }

        /*
         * Create mappings only for permissions that have never
         * been assigned to this role.
         */
        for (Permission permission : selectedPermissions.values()) {

            if (!existingByPermissionId.containsKey(permission.getId())) {

                RolePermission mapping = new RolePermission();

                mapping.setRole(role);
                mapping.setPermission(permission);
                mapping.setIsActive(ACTIVE);
                mapping.setIsDeleted(NOT_DELETED);
                mapping.setCreatedBy(currentUserId);

                mappingsToSave.add(mapping);
            }
        }

        rolePermissionRepository.saveAll(mappingsToSave);

        log.info(
                "Role permissions saved successfully. rolePublicId={}, selectedCount={}",
                rolePublicId,
                selectedPermissions.size()
        );

        return getByRolePublicId(rolePublicId);
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Role Permission | publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        RolePermission entity =
                rolePermissionValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        entity.markAsDeleted(
                currentUserId
        );

        rolePermissionRepository.save(
                entity
        );

        log.info(
                "Role Permission deleted successfully | publicId={}",
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
                "Activating Role Permission | publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        RolePermission entity =
                rolePermissionValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        entity.activate(
                currentUserId
        );

        rolePermissionRepository.save(
                entity
        );

        log.info(
                "Role Permission activated successfully | publicId={}",
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
                "Deactivating Role Permission | publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        RolePermission entity =
                rolePermissionValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        entity.deactivate(
                currentUserId
        );

        rolePermissionRepository.save(
                entity
        );

        log.info(
                "Role Permission deactivated successfully | publicId={}",
                publicId
        );
    }

    /**
     * Validates that the current user is allowed to manage
     * role-permission mappings.
     *
     * <p>
     * Role-permission mappings are platform-level security
     * configuration and therefore can only be managed by
     * SUPER_ADMIN.
     * </p>
     */
    private void validateSuperAdminAccess() {

        if (!currentUserService.isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required to manage role permissions."
            );
        }

        if (!currentUserService.isSuperAdmin()) {

            throw new ForbiddenException(
                    "Only SUPER_ADMIN can manage role permissions."
            );
        }
    }


    /**
     * Validates the role that can receive shared permissions.
     *
     * <p>SUPER_ADMIN configures permissions for the shared ADMIN
     * role. All client Admins and their users inherit the permissions
     * assigned to this role.</p>
     *
     * <p>Individual user page visibility is managed separately through
     * UserMenu and is not configured through RolePermission.</p>
     *
     * @param role role receiving the permission assignments
     */
    private void validateRoleAssignment(Role role) {

        if (role == null) {
            throw new BadRequestException(
                    "Role is required."
            );
        }

        String roleCode = role.getRoleCode();

        if (roleCode == null
                || !RoleConstants.ADMIN.equalsIgnoreCase(roleCode.trim())) {

            throw new ForbiddenException(
                    "Shared permissions can only be assigned to the ADMIN role."
            );
        }
    }

}