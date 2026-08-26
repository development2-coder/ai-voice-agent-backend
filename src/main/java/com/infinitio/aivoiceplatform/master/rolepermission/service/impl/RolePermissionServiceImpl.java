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

        rolePermissionValidator.validateForCreate(
                request
        );

        Role role =
                roleValidator.validateAndGet(
                        request
                                .getRolePublicId()
                                .trim()
                );

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
    public PageResponse<RolePermissionResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Role Permissions | page={} | size={}",
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

        Page<RolePermission> result =
                rolePermissionRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<RolePermissionResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        rolePermissionMapper::toResponse
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

        log.info(
                "Deleting Role Permission | publicId={}",
                publicId
        );

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
}