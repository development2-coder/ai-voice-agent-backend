package com.infinitio.aivoiceplatform.master.permission.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.master.permission.constant.PermissionConstants;
import com.infinitio.aivoiceplatform.master.permission.dto.request.CreatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.dto.request.UpdatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.dto.response.PermissionResponse;
import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import com.infinitio.aivoiceplatform.master.permission.mapper.PermissionMapper;
import com.infinitio.aivoiceplatform.master.permission.repository.PermissionRepository;
import com.infinitio.aivoiceplatform.master.permission.service.PermissionService;
import com.infinitio.aivoiceplatform.master.permission.validator.PermissionValidator;
import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
import com.infinitio.aivoiceplatform.master.platformmodule.repository.PlatformModuleRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
/**
 * Permission Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl
        implements PermissionService {

    private static final Integer ACTIVE =
            PermissionConstants.ACTIVE;

    private static final Integer NOT_DELETED =
            0;

    private final PermissionRepository permissionRepository;

    private final PermissionMapper permissionMapper;

    private final PermissionValidator permissionValidator;

    private final CurrentUserService currentUserService;

    private final PlatformModuleRepository platformModuleRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public PermissionResponse create(
            CreatePermissionRequest request) {

        log.info(
                "Creating Permission. permissionCode={}",
                request != null
                        ? request.getPermissionCode()
                        : null
        );

        validateSuperAdminAccess();

        permissionValidator.validateForCreate(
                request
        );

        Permission permission =
                permissionMapper.toEntity(
                        request
                );

        /*
         * Defaults.
         */
        permission.setDisplayOrder(
                request.getDisplayOrder() != null
                        ? request.getDisplayOrder()
                        : PermissionConstants
                        .DEFAULT_DISPLAY_ORDER
        );

        permission.setIsSystem(
                request.getIsSystem() != null
                        ? request.getIsSystem()
                        : PermissionConstants
                        .CUSTOM_PERMISSION
        );

        permission.setIsActive(
                ACTIVE
        );

        permission.setIsDeleted(
                NOT_DELETED
        );

        /*
         * Audit.
         */
        permission.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        PlatformModule module = platformModuleRepository
                .findByPublicIdAndIsDeleted(
                        request.getModulePublicId().trim(),
                        0
                )
                .orElseThrow(() ->
                        new BadRequestException("Platform module not found.")
                );

        if (!Integer.valueOf(1).equals(module.getIsActive())) {
            throw new BadRequestException("Platform module is inactive.");
        }

        permission.setModule(module);

        Permission savedPermission =
                permissionRepository.save(
                        permission
                );

        log.info(
                "Permission created successfully. publicId={}",
                savedPermission.getPublicId()
        );

        return permissionMapper.toResponse(
                savedPermission
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public PermissionResponse update(
            UpdatePermissionRequest request) {

        log.info(
                "Updating Permission. publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        validateSuperAdminAccess();

        permissionValidator.validateForUpdate(
                request
        );

        Permission existingPermission =
                permissionValidator.validateAndGet(
                        request.getPublicId().trim()
                );

        validateSystemPermissionModification(
                existingPermission
        );

        permissionMapper.updateEntityFromRequest(
                request,
                existingPermission
        );

        existingPermission.setIsDeleted(
                NOT_DELETED
        );

        existingPermission.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        Permission updatedPermission =
                permissionRepository.save(
                        existingPermission
                );

        log.info(
                "Permission updated successfully. publicId={}",
                updatedPermission.getPublicId()
        );

        return permissionMapper.toResponse(
                updatedPermission
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Permission. publicId={}",
                publicId
        );

        Permission permission =
                permissionValidator.validateAndGet(
                        publicId
                );

        return permissionMapper.toResponse(
                permission
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAll() {

        log.info("Fetching all Permissions.");

        return permissionRepository
                .findByIsDeletedOrderByDisplayOrderAsc(
                        NOT_DELETED
                )
                .stream()
                .map(permissionMapper::toResponse)
                .toList();
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Permission. publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        Permission permission =
                permissionValidator.validateAndGet(
                        publicId
                );

        validateSystemPermissionModification(
                permission
        );

        permission.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        permissionRepository.save(
                permission
        );

        log.info(
                "Permission deleted successfully. publicId={}",
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
                "Activating Permission. publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        Permission permission =
                permissionValidator.validateAndGet(
                        publicId
                );

        validateSystemPermissionModification(
                permission
        );

        permission.activate(
                currentUserService.getCurrentUserId()
        );

        permissionRepository.save(
                permission
        );

        log.info(
                "Permission activated successfully. publicId={}",
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
                "Deactivating Permission. publicId={}",
                publicId
        );

        validateSuperAdminAccess();

        Permission permission =
                permissionValidator.validateAndGet(
                        publicId
                );

        validateSystemPermissionModification(
                permission
        );

        permission.deactivate(
                currentUserService.getCurrentUserId()
        );

        permissionRepository.save(
                permission
        );

        log.info(
                "Permission deactivated successfully. publicId={}",
                publicId
        );
    }

    // =========================================================
// AUTHORIZATION
// =========================================================

    /**
     * Validates that the current user is SUPER_ADMIN.
     *
     * <p>
     * Permission definitions are platform-level security
     * configuration and can only be managed by SUPER_ADMIN.
     * </p>
     */
    private void validateSuperAdminAccess() {

        if (!currentUserService.isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required to manage permissions."
            );
        }

        if (!currentUserService.isSuperAdmin()) {

            throw new ForbiddenException(
                    "Only SUPER_ADMIN can manage permissions."
            );
        }
    }


// =========================================================
// SYSTEM PERMISSION PROTECTION
// =========================================================

    /**
     * Prevents modification of system permissions.
     *
     * @param permission permission being modified
     */
    private void validateSystemPermissionModification(
            Permission permission) {

        if (permission == null) {

            throw new ForbiddenException(
                    "Permission is required."
            );
        }

        if (permission.getIsSystem() != null
                && permission.getIsSystem()
                == PermissionConstants.SYSTEM_PERMISSION) {

            throw new ForbiddenException(
                    "System permission cannot be modified."
            );
        }
    }
}