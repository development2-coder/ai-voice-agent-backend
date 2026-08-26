package com.infinitio.aivoiceplatform.master.platformmodule.service.impl;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.CreatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.UpdatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.response.PlatformModuleResponse;
import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
import com.infinitio.aivoiceplatform.master.platformmodule.mapper.PlatformModuleMapper;
import com.infinitio.aivoiceplatform.master.platformmodule.repository.PlatformModuleRepository;
import com.infinitio.aivoiceplatform.master.platformmodule.service.PlatformModuleService;
import com.infinitio.aivoiceplatform.master.platformmodule.validator.PlatformModuleValidator;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
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


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public PlatformModuleResponse create(
            CreatePlatformModuleRequest request) {

        log.info(
                "Creating Platform Module. moduleCode={}",
                request != null
                        ? request.getModuleCode()
                        : null
        );

        /*
         * Validate request and duplicate values.
         */
        platformModuleValidator.validateForCreate(
                request
        );

        /*
         * Convert request to entity.
         */
        PlatformModule platformModule =
                platformModuleMapper.toEntity(
                        request
                );

        /*
         * New platform modules are active
         * and non-deleted by default.
         *
         * BaseEntity also provides these defaults
         * during @PrePersist, but setting them here
         * makes the service behaviour explicit.
         */
        platformModule.setIsActive(
                ACTIVE
        );

        platformModule.setIsDeleted(
                NOT_DELETED
        );

        /*
         * Persist entity.
         */
        PlatformModule savedModule =
                platformModuleRepository.save(
                        platformModule
                );

        log.info(
                "Platform Module created successfully. publicId={}",
                savedModule.getPublicId()
        );

        return platformModuleMapper.toResponse(
                savedModule
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public PlatformModuleResponse update(
            UpdatePlatformModuleRequest request) {

        log.info(
                "Updating Platform Module. publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        /*
         * Validate request and duplicate values.
         */
        platformModuleValidator.validateForUpdate(
                request
        );

        /*
         * Get existing non-deleted module.
         */
        PlatformModule existingModule =
                platformModuleValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * Update only fields supplied by request.
         *
         * Mapper uses:
         * NullValuePropertyMappingStrategy.IGNORE
         */
        platformModuleMapper.updateEntityFromRequest(
                request,
                existingModule
        );

        /*
         * Persist updated module.
         */
        PlatformModule updatedModule =
                platformModuleRepository.save(
                        existingModule
                );

        log.info(
                "Platform Module updated successfully. publicId={}",
                updatedModule.getPublicId()
        );

        return platformModuleMapper.toResponse(
                updatedModule
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PlatformModuleResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Platform Module. publicId={}",
                publicId
        );

        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(
                        publicId
                );

        return platformModuleMapper.toResponse(
                platformModule
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

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
         * IMPORTANT:
         *
         * Do not use repository.findAll().
         *
         * findAll() would also return soft-deleted
         * platform modules.
         *
         * Only records with:
         *
         * is_deleted = 0
         *
         * are returned.
         */
        Page<PlatformModule> modulePage =
                platformModuleRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<PlatformModuleResponse>builder()
                .content(
                        modulePage
                                .getContent()
                                .stream()
                                .map(
                                        platformModuleMapper::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        modulePage.getNumber()
                )
                .pageSize(
                        modulePage.getSize()
                )
                .totalElements(
                        modulePage.getTotalElements()
                )
                .totalPages(
                        modulePage.getTotalPages()
                )
                .first(
                        modulePage.isFirst()
                )
                .last(
                        modulePage.isLast()
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
                "Deleting Platform Module. publicId={}",
                publicId
        );

        /*
         * Only non-deleted modules can be deleted.
         */
        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(
                        publicId
                );

        /*
         * Soft delete.
         *
         * This also:
         * - sets isActive = 0
         * - sets isDeleted = 1
         * - sets deletedAt
         */
        platformModule.markAsDeleted(
                1L
        );

        platformModuleRepository.save(
                platformModule
        );

        log.info(
                "Platform Module deleted successfully. publicId={}",
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
                "Activating Platform Module. publicId={}",
                publicId
        );

        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(
                        publicId
                );

        /*
         * Activate the module.
         *
         * Do not activate a deleted module because
         * validateAndGet() only returns isDeleted = 0.
         */
        platformModule.activate(
                1L
        );

        platformModuleRepository.save(
                platformModule
        );

        log.info(
                "Platform Module activated successfully. publicId={}",
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
                "Deactivating Platform Module. publicId={}",
                publicId
        );

        PlatformModule platformModule =
                platformModuleValidator.validateAndGet(
                        publicId
                );

        /*
         * Deactivate without deleting.
         *
         * Result:
         * isActive = 0
         * isDeleted = 0
         */
        platformModule.deactivate(
                1L
        );

        platformModuleRepository.save(
                platformModule
        );

        log.info(
                "Platform Module deactivated successfully. publicId={}",
                publicId
        );
    }
}