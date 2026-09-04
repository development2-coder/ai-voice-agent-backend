package com.infinitio.aivoiceplatform.master.role.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.role.dto.request.CreateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.request.UpdateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.response.RoleResponse;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.mapper.RoleMapper;
import com.infinitio.aivoiceplatform.master.role.repository.RoleRepository;
import com.infinitio.aivoiceplatform.master.role.service.RoleService;
import com.infinitio.aivoiceplatform.master.role.validator.RoleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role Service Implementation.
 *
 * Handles role creation, update, retrieval, deletion,
 * activation and deactivation operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Integer ACTIVE = 1;
    private static final Integer NOT_DELETED = 0;

    private final RoleRepository roleRepository;

    private final RoleMapper roleMapper;

    private final RoleValidator roleValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Creates a new role.
     *
     * @param request role creation request
     * @return created role response
     */
    @Override
    public RoleResponse create(
            CreateRoleRequest request) {

        log.info(
                "Creating Role : {}",
                request.getRoleCode()
        );

        roleValidator.validateForCreate(
                request
        );

        Role role =
                roleMapper.toEntity(
                        request
                );

        /*
         * Get the authenticated user who is creating
         * the role.
         *
         * createdBy is mandatory in BaseEntity/database,
         * therefore it must be populated before save.
         */
        Long currentUserId =
                currentUserService.getCurrentUserId();

        role.setCreatedBy(
                currentUserId
        );

        /*
         * Defaults for a newly created role.
         */
        if (role.getDisplayOrder() == null) {
            role.setDisplayOrder(1);
        }

        if (role.getIsSystem() == null) {
            role.setIsSystem(0);
        }

        if (role.getIsDefault() == null) {
            role.setIsDefault(0);
        }

        role.setIsActive(
                ACTIVE
        );

        role.setIsDeleted(
                NOT_DELETED
        );

        Role savedRole =
                roleRepository.save(
                        role
                );

        log.info(
                "Role created successfully : publicId={}, createdBy={}",
                savedRole.getPublicId(),
                currentUserId
        );

        return roleMapper.toResponse(
                savedRole
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Updates an existing role.
     *
     * @param request role update request
     * @return updated role response
     */
    @Override
    public RoleResponse update(
            UpdateRoleRequest request) {

        log.info(
                "Updating Role : {}",
                request.getPublicId()
        );

        /*
         * Validate request and duplicate values.
         */
        roleValidator.validateForUpdate(
                request
        );

        /*
         * Get only non-deleted role.
         */
        Role existingRole =
                roleValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * Update only allowed request fields.
         *
         * RoleMapper intentionally ignores createdBy
         * so the original creator is preserved.
         */
        roleMapper.updateEntity(
                request,
                existingRole
        );

        /*
         * Store the authenticated user who performed
         * the update.
         */
        Long currentUserId =
                currentUserService.getCurrentUserId();

        existingRole.setUpdatedBy(
                currentUserId
        );

        Role updatedRole =
                roleRepository.save(
                        existingRole
                );

        log.info(
                "Role updated successfully : publicId={}, updatedBy={}",
                updatedRole.getPublicId(),
                currentUserId
        );

        return roleMapper.toResponse(
                updatedRole
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Retrieves a role by public ID.
     *
     * @param publicId role public ID
     * @return role response
     */
    @Override
    @Transactional(readOnly = true)
    public RoleResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Role : {}",
                publicId
        );

        Role role =
                roleValidator.validateAndGet(
                        publicId
                );

        return roleMapper.toResponse(
                role
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Retrieves all non-deleted roles.
     *
     * @param page page number
     * @param size page size
     * @return paginated role response
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Roles. Page : {}, Size : {}",
                page,
                size
        );

        Page<Role> rolePage =
                roleRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse.<RoleResponse>builder()
                .content(
                        rolePage
                                .getContent()
                                .stream()
                                .map(
                                        roleMapper::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        rolePage.getNumber()
                )
                .pageSize(
                        rolePage.getSize()
                )
                .totalElements(
                        rolePage.getTotalElements()
                )
                .totalPages(
                        rolePage.getTotalPages()
                )
                .first(
                        rolePage.isFirst()
                )
                .last(
                        rolePage.isLast()
                )
                .build();
    }


    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Soft deletes a role.
     *
     * @param publicId role public ID
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Role : {}",
                publicId
        );

        Role role =
                roleValidator.validateAndGet(
                        publicId
                );

        /*
         * Do not allow deletion of a system role.
         */
        if (role.getIsSystem() != null
                && role.getIsSystem() == ACTIVE) {

            throw new IllegalStateException(
                    "System role cannot be deleted."
            );
        }

        Long currentUserId =
                currentUserService.getCurrentUserId();

        role.markAsDeleted(
                currentUserId
        );

        roleRepository.save(
                role
        );

        log.info(
                "Role deleted successfully : publicId={}, deletedBy={}",
                publicId,
                currentUserId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activates a role.
     *
     * @param publicId role public ID
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Role : {}",
                publicId
        );

        Role role =
                roleValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        role.activate(
                currentUserId
        );

        roleRepository.save(
                role
        );

        log.info(
                "Role activated successfully : publicId={}, updatedBy={}",
                publicId,
                currentUserId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivates a role.
     *
     * @param publicId role public ID
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Role : {}",
                publicId
        );

        Role role =
                roleValidator.validateAndGet(
                        publicId
                );

        /*
         * A system role should not accidentally be
         * disabled.
         */
        if (role.getIsSystem() != null
                && role.getIsSystem() == ACTIVE) {

            throw new IllegalStateException(
                    "System role cannot be deactivated."
            );
        }

        Long currentUserId =
                currentUserService.getCurrentUserId();

        role.deactivate(
                currentUserId
        );

        roleRepository.save(
                role
        );

        log.info(
                "Role deactivated successfully : publicId={}, updatedBy={}",
                publicId,
                currentUserId
        );
    }
}