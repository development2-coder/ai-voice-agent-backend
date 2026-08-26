package com.infinitio.aivoiceplatform.master.role.service.impl;

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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Integer ACTIVE = 1;
    private static final Integer INACTIVE = 0;
    private static final Integer NOT_DELETED = 0;
    private static final Integer DELETED = 1;

    private final RoleRepository roleRepository;

    private final RoleMapper roleMapper;

    private final RoleValidator roleValidator;


    // =========================================================
    // CREATE
    // =========================================================

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
                "Role created successfully : {}",
                savedRole.getPublicId()
        );

        return roleMapper.toResponse(
                savedRole
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

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
         */
        roleMapper.updateEntity(
                request,
                existingRole
        );

        Role updatedRole =
                roleRepository.save(
                        existingRole
                );

        log.info(
                "Role updated successfully : {}",
                updatedRole.getPublicId()
        );

        return roleMapper.toResponse(
                updatedRole
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

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

        role.markAsDeleted(
                1L
        );

        roleRepository.save(
                role
        );

        log.info(
                "Role deleted successfully : {}",
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
                "Activating Role : {}",
                publicId
        );

        Role role =
                roleValidator.validateAndGet(
                        publicId
                );

        role.activate(
                1L
        );

        roleRepository.save(
                role
        );

        log.info(
                "Role activated successfully : {}",
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

        role.deactivate(
                1L
        );

        roleRepository.save(
                role
        );

        log.info(
                "Role deactivated successfully : {}",
                publicId
        );
    }
}