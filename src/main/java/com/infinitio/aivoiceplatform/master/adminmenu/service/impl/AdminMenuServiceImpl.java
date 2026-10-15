package com.infinitio.aivoiceplatform.master.adminmenu.service.impl;

import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.master.adminmenu.constant.AdminMenuConstants;
import com.infinitio.aivoiceplatform.master.adminmenu.constant.AdminMenuMessages;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.CreateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.UpdateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.response.AdminMenuResponse;
import com.infinitio.aivoiceplatform.master.adminmenu.entity.AdminMenu;
import com.infinitio.aivoiceplatform.master.adminmenu.mapper.AdminMenuMapper;
import com.infinitio.aivoiceplatform.master.adminmenu.repository.AdminMenuRepository;
import com.infinitio.aivoiceplatform.master.adminmenu.service.AdminMenuService;
import com.infinitio.aivoiceplatform.master.adminmenu.validator.AdminMenuValidator;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.validator.MenuValidator;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.organization.tenant.validator.TenantValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ADMIN menu service implementation.
 *
 * <p>
 * Only SUPER_ADMIN can assign platform pages to an ADMIN/client.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminMenuServiceImpl
        implements AdminMenuService {

    private final AdminMenuRepository adminMenuRepository;

    private final AdminMenuMapper adminMenuMapper;

    private final AdminMenuValidator adminMenuValidator;

    private final TenantValidator tenantValidator;

    private final MenuValidator menuValidator;

    private final TenantAccessService tenantAccessService;


    @Override
    public AdminMenuResponse create(
            CreateAdminMenuRequest request) {

        tenantAccessService.validateSuperAdminAccess();

        adminMenuValidator.validateForCreate(
                request
        );

        Tenant admin =
                tenantValidator.validateAndGet(
                        request.getAdminPublicId()
                );

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId()
                );

        /*
         * This relationship represents an ADMIN/client.
         * SUPER_ADMIN is the only role allowed to configure it.
         */
        if (adminMenuRepository
                .existsByAdmin_IdAndMenu_IdAndIsDeleted(
                        admin.getId(),
                        menu.getId(),
                        AdminMenuConstants.NOT_DELETED
                )) {

            throw new ConflictException(
                    AdminMenuMessages.ALREADY_EXISTS
            );
        }

        AdminMenu adminMenu =
                adminMenuMapper.toEntity(
                        request
                );

        adminMenu.setAdmin(admin);
        adminMenu.setMenu(menu);

        if (adminMenu.getIsVisible() == null) {
            adminMenu.setIsVisible(
                    AdminMenuConstants.VISIBLE
            );
        }

        adminMenu.setCreatedBy(
                tenantAccessService.getCurrentUserId()
        );

        AdminMenu saved =
                adminMenuRepository.save(
                        adminMenu
                );

        return adminMenuMapper.toResponse(
                saved
        );
    }


    @Override
    public AdminMenuResponse update(
            UpdateAdminMenuRequest request) {

        tenantAccessService.validateSuperAdminAccess();

        adminMenuValidator.validateForUpdate(
                request
        );

        AdminMenu adminMenu =
                adminMenuValidator.validateAndGet(
                        request.getPublicId()
                );

        Tenant admin =
                tenantValidator.validateAndGet(
                        request.getAdminPublicId()
                );

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId()
                );

        if (adminMenuRepository
                .existsByAdmin_IdAndMenu_IdAndIsDeletedAndIdNot(
                        admin.getId(),
                        menu.getId(),
                        AdminMenuConstants.NOT_DELETED,
                        adminMenu.getId()
                )) {

            throw new ConflictException(
                    AdminMenuMessages.ALREADY_EXISTS
            );
        }

        adminMenuMapper.updateEntity(
                request,
                adminMenu
        );

        adminMenu.setAdmin(admin);
        adminMenu.setMenu(menu);

        if (adminMenu.getIsVisible() == null) {
            adminMenu.setIsVisible(
                    AdminMenuConstants.VISIBLE
            );
        }

        adminMenu.setUpdatedBy(
                tenantAccessService.getCurrentUserId()
        );

        return adminMenuMapper.toResponse(
                adminMenuRepository.save(
                        adminMenu
                )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public AdminMenuResponse getByPublicId(
            String publicId) {

        tenantAccessService.validateSuperAdminAccess();

        AdminMenu adminMenu =
                adminMenuValidator.validateAndGet(
                        publicId
                );

        return adminMenuMapper.toResponse(
                adminMenu
        );
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminMenuResponse> getAll(
            int page,
            int size) {

        tenantAccessService.validateSuperAdminAccess();

        Page<AdminMenu> result =
                adminMenuRepository.findByIsDeleted(
                        AdminMenuConstants.NOT_DELETED,
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<AdminMenuResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        adminMenuMapper::toResponse
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


    @Override
    public void delete(
            String publicId) {

        tenantAccessService.validateSuperAdminAccess();

        AdminMenu adminMenu =
                adminMenuValidator.validateAndGet(
                        publicId
                );

        adminMenu.markAsDeleted(
                tenantAccessService.getCurrentUserId()
        );

        adminMenuRepository.save(
                adminMenu
        );
    }


    @Override
    public void activate(
            String publicId) {

        tenantAccessService.validateSuperAdminAccess();

        AdminMenu adminMenu =
                adminMenuValidator.validateAndGet(
                        publicId
                );

        adminMenu.activate(
                tenantAccessService.getCurrentUserId()
        );

        adminMenuRepository.save(
                adminMenu
        );
    }


    @Override
    public void deactivate(
            String publicId) {

        tenantAccessService.validateSuperAdminAccess();

        AdminMenu adminMenu =
                adminMenuValidator.validateAndGet(
                        publicId
                );

        adminMenu.deactivate(
                tenantAccessService.getCurrentUserId()
        );

        adminMenuRepository.save(
                adminMenu
        );
    }
}