package com.infinitio.aivoiceplatform.organization.organizationstatus.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.organization.organizationstatus.constant.OrganizationStatusConstants;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.CreateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.UpdateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.response.OrganizationStatusResponse;
import com.infinitio.aivoiceplatform.organization.organizationstatus.entity.OrganizationStatus;
import com.infinitio.aivoiceplatform.organization.organizationstatus.mapper.OrganizationStatusMapper;
import com.infinitio.aivoiceplatform.organization.organizationstatus.repository.OrganizationStatusRepository;
import com.infinitio.aivoiceplatform.organization.organizationstatus.service.OrganizationStatusService;
import com.infinitio.aivoiceplatform.organization.organizationstatus.validator.OrganizationStatusValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Organization Status Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationStatusServiceImpl
        implements OrganizationStatusService {

    private final OrganizationStatusRepository repository;

    private final OrganizationStatusMapper mapper;

    private final OrganizationStatusValidator validator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public OrganizationStatusResponse create(
            CreateOrganizationStatusRequest request) {

        log.info(
                "Creating organization status. code={}",
                request.getOrganizationStatusCode()
        );

        validator.validateForCreate(request);

        OrganizationStatus entity =
                mapper.toEntity(request);

        entity.setOrganizationStatusCode(
                request.getOrganizationStatusCode()
                        .trim()
                        .toUpperCase()
        );

        entity.setOrganizationStatusName(
                request.getOrganizationStatusName()
                        .trim()
        );

        entity.setIsActive(
                OrganizationStatusConstants.ACTIVE
        );

        entity.setIsDeleted(
                OrganizationStatusConstants.NOT_DELETED
        );

        entity.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        OrganizationStatus saved =
                repository.save(entity);

        log.info(
                "Organization status created successfully. publicId={}",
                saved.getPublicId()
        );

        return mapper.toResponse(saved);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public OrganizationStatusResponse update(
            UpdateOrganizationStatusRequest request) {

        log.info(
                "Updating organization status. publicId={}",
                request.getPublicId()
        );

        validator.validateForUpdate(request);

        OrganizationStatus entity =
                validator.validateAndGet(
                        request.getPublicId()
                );

        mapper.updateEntity(
                request,
                entity
        );

        entity.setOrganizationStatusCode(
                request.getOrganizationStatusCode()
                        .trim()
                        .toUpperCase()
        );

        entity.setOrganizationStatusName(
                request.getOrganizationStatusName()
                        .trim()
        );

        entity.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        OrganizationStatus updated =
                repository.save(entity);

        log.info(
                "Organization status updated successfully. publicId={}",
                updated.getPublicId()
        );

        return mapper.toResponse(updated);
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public OrganizationStatusResponse getByPublicId(
            String publicId) {

        return mapper.toResponse(
                validator.validateAndGet(publicId)
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationStatusResponse> getAll(
            int page,
            int size) {

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

        Page<OrganizationStatus> result =
                repository.findByIsDeleted(
                        OrganizationStatusConstants.NOT_DELETED,
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<OrganizationStatusResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(mapper::toResponse)
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

        OrganizationStatus entity =
                validator.validateAndGet(publicId);

        entity.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        repository.save(entity);

        log.info(
                "Organization status deleted successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        OrganizationStatus entity =
                validator.validateAndGet(publicId);

        entity.activate(
                currentUserService.getCurrentUserId()
        );

        repository.save(entity);

        log.info(
                "Organization status activated successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        OrganizationStatus entity =
                validator.validateAndGet(publicId);

        entity.deactivate(
                currentUserService.getCurrentUserId()
        );

        repository.save(entity);

        log.info(
                "Organization status deactivated successfully. publicId={}",
                publicId
        );
    }
}