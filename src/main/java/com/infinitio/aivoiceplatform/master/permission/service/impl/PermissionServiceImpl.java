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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        permissionValidator.validateForUpdate(
                request
        );

        Permission existingPermission =
                permissionValidator.validateAndGet(
                        request.getPublicId().trim()
                );

        permissionMapper.updateEntityFromRequest(
                request,
                existingPermission
        );

        /*
         * Preserve lifecycle state.
         */
        existingPermission.setIsDeleted(
                NOT_DELETED
        );

        /*
         * Audit.
         */
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
    public PageResponse<PermissionResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Permissions. page={}, size={}",
                page,
                size
        );

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

        Page<Permission> permissionPage =
                permissionRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<PermissionResponse>builder()
                .content(
                        permissionPage
                                .getContent()
                                .stream()
                                .map(
                                        permissionMapper::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        permissionPage.getNumber()
                )
                .pageSize(
                        permissionPage.getSize()
                )
                .totalElements(
                        permissionPage.getTotalElements()
                )
                .totalPages(
                        permissionPage.getTotalPages()
                )
                .first(
                        permissionPage.isFirst()
                )
                .last(
                        permissionPage.isLast()
                )
                .build();
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

        Permission permission =
                permissionValidator.validateAndGet(
                        publicId
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

        Permission permission =
                permissionValidator.validateAndGet(
                        publicId
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

        Permission permission =
                permissionValidator.validateAndGet(
                        publicId
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
}