package com.infinitio.aivoiceplatform.master.platformmodule.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.CreatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.UpdatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.response.PlatformModuleResponse;
import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
import com.infinitio.aivoiceplatform.master.platformmodule.mapper.PlatformModuleMapper;
import com.infinitio.aivoiceplatform.master.platformmodule.repository.PlatformModuleRepository;
import com.infinitio.aivoiceplatform.master.platformmodule.service.PlatformModuleService;
import com.infinitio.aivoiceplatform.master.platformmodule.validator.PlatformModuleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Platform Module Service Implementation.
 *
 * Handles platform module business operations.
 *
 * Platform modules are system-level configuration and therefore
 * can only be managed by SUPER_ADMIN.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlatformModuleServiceImpl
        implements PlatformModuleService {

    private static final Integer ACTIVE = 1;

    private static final Integer INACTIVE = 0;

    private static final Integer NOT_DELETED = 0;

    private final PlatformModuleRepository platformModuleRepository;

    private final PlatformModuleMapper platformModuleMapper;

    private final PlatformModuleValidator platformModuleValidator;

    private final CurrentUserService currentUserService;

    // =========================================================
    // AUTHORIZATION
    // =========================================================

    /**
     * Validate that the current user is authenticated and is
     * a SUPER_ADMIN.
     *
     * Platform modules are system-level configuration and must
     * not be managed by ADMIN or USER.
     */
    private void validateSuperAdminAccess() {

        if (!currentUserService.isAuthenticated()) {
            throw new ForbiddenException(
                    "Authentication is required to manage platform modules."
            );
        }

        if (!currentUserService.isSuperAdmin()) {
            throw new ForbiddenException(
                    "Only Super Admin can manage platform modules."
            );
        }
    }

    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Create a new Platform Module.
     *
     * @param request create request
     * @return created Platform Module
     */
    @Override
    public PlatformModuleResponse create(
            CreatePlatformModuleRequest request) {

        validateSuperAdminAccess();

        log.info(
                "Creating Platform Module. moduleCode={}",
                request != null
                        ? request.getModuleCode()
                        : null
        );

        /*
         * Validate request and duplicate values.
         */
        platformModuleValidator.validateForCreate(request);

        /*
         * Convert request to entity.
         */
        PlatformModule platformModule =
                platformModuleMapper.toEntity(request);

        /*
         * New platform modules are active
         * and non-deleted by default.
         */
        platformModule.setIsActive(ACTIVE);

        platformModule.setIsDeleted(NOT_DELETED);

        /*
         * Persist entity.
         */
        PlatformModule savedModule =
                platformModuleRepository.save(platformModule);

        log.info(
                "Platform Module created successfully. publicId={}",
                savedModule.getPublicId()
        );

        return platformModuleMapper.toResponse(savedModule);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Update an existing Platform Module.
     *
     * @param request update request
     * @return updated Platform Module
     */
    @Override
    public PlatformModuleResponse update(
            UpdatePlatformModuleRequest request) {

        validateSuperAdminAccess();

        log.info(
                "Updating Platform Module. publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        /*
         * Validate request and duplicate values.
         */
        platformModuleValidator.validateForUpdate(request);

        /*
         * Get existing non-deleted module.
         */
        PlatformModule existingModule =
                platformModuleValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * Update only fields supplied by request.
         */
        platformModuleMapper.updateEntityFromRequest(
                request,
                existingModule
        );

        /*
         * Persist updated module.
         */
        PlatformModule updatedModule =
                platformModuleRepository.save(existingModule);

        log.info(
                "Platform Module updated successfully. publicId={}",
                updatedModule.getPublicId()
        );

        return platformModuleMapper.toResponse(updatedModule);
    }

    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Get Platform Module by public ID.
     *
     * @param publicId public UUID
     * @return Platform Module
     */
    @Override
    @Transactional(readOnly = true)
    public PlatformModuleResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Platform Module. publicId={}",
                publicId
        );

        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(publicId);

        return platformModuleMapper.toResponse(platformModule);
    }

    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Get all non-deleted Platform Modules.
     *
     * @param page page number
     * @param size page size
     * @return paginated Platform Modules
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlatformModuleResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Platform Modules. page={}, size={}",
                page,
                size
        );

        /*
         * Validate pagination.
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
         * Only non-deleted platform modules are returned.
         */
        Page<PlatformModule> modulePage =
                platformModuleRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<PlatformModuleResponse>builder()
                .content(
                        modulePage
                                .getContent()
                                .stream()
                                .map(platformModuleMapper::toResponse)
                                .toList()
                )
                .pageNumber(modulePage.getNumber())
                .pageSize(modulePage.getSize())
                .totalElements(modulePage.getTotalElements())
                .totalPages(modulePage.getTotalPages())
                .first(modulePage.isFirst())
                .last(modulePage.isLast())
                .build();
    }

    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Soft delete a Platform Module.
     *
     * @param publicId public UUID
     */
    @Override
    public void delete(String publicId) {

        validateSuperAdminAccess();

        log.info(
                "Deleting Platform Module. publicId={}",
                publicId
        );

        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(publicId);

        /*
         * Soft delete.
         */
        platformModule.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        platformModuleRepository.save(platformModule);

        log.info(
                "Platform Module deleted successfully. publicId={}",
                publicId
        );
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activate a Platform Module.
     *
     * @param publicId public UUID
     */
    @Override
    public void activate(String publicId) {

        validateSuperAdminAccess();

        log.info(
                "Activating Platform Module. publicId={}",
                publicId
        );

        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(publicId);

        /*
         * Deleted modules cannot be activated because
         * validateAndGet() only returns non-deleted records.
         */
        platformModule.activate(
                currentUserService.getCurrentUserId()
        );

        platformModuleRepository.save(platformModule);

        log.info(
                "Platform Module activated successfully. publicId={}",
                publicId
        );
    }

    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivate a Platform Module.
     *
     * @param publicId public UUID
     */
    @Override
    public void deactivate(String publicId) {

        validateSuperAdminAccess();

        log.info(
                "Deactivating Platform Module. publicId={}",
                publicId
        );

        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(publicId);

        /*
         * Deactivate without deleting.
         *
         * Result:
         * isActive = 0
         * isDeleted = 0
         */
        platformModule.deactivate(
                currentUserService.getCurrentUserId()
        );

        platformModuleRepository.save(platformModule);

        log.info(
                "Platform Module deactivated successfully. publicId={}",
                publicId
        );
    }
}