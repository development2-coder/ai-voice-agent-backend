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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PageResponse<RoleMenuResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Role Menus | page={} | size={}",
                page,
                size
        );

        /*
         * Pagination validation.
         */
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

        /*
         * Only non-deleted mappings.
         */
        Page<RoleMenu> result =
                roleMenuRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<RoleMenuResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        roleMenuMapper::toResponse
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
                "Deleting Role Menu | publicId={}",
                publicId
        );

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
}