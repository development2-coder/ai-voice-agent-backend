package com.infinitio.aivoiceplatform.organization.organizationtype.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.organization.organizationtype.constant.OrganizationTypeConstants;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.CreateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.UpdateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.response.OrganizationTypeResponse;
import com.infinitio.aivoiceplatform.organization.organizationtype.entity.OrganizationType;
import com.infinitio.aivoiceplatform.organization.organizationtype.mapper.OrganizationTypeMapper;
import com.infinitio.aivoiceplatform.organization.organizationtype.repository.OrganizationTypeRepository;
import com.infinitio.aivoiceplatform.organization.organizationtype.service.OrganizationTypeService;
import com.infinitio.aivoiceplatform.organization.organizationtype.validator.OrganizationTypeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Organization Type Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationTypeServiceImpl
        implements OrganizationTypeService {

    private final OrganizationTypeRepository repository;

    private final OrganizationTypeMapper mapper;

    private final OrganizationTypeValidator validator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public OrganizationTypeResponse create(
            CreateOrganizationTypeRequest request) {

        log.info(
                "Creating organization type. code={}",
                request.getOrganizationTypeCode()
        );

        validator.validateForCreate(request);

        OrganizationType entity =
                mapper.toEntity(request);

        /*
         * Normalize values before persistence.
         */
        entity.setOrganizationTypeCode(
                request.getOrganizationTypeCode()
                        .trim()
                        .toUpperCase()
        );

        entity.setOrganizationTypeName(
                request.getOrganizationTypeName()
                        .trim()
        );

        entity.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        entity.setIsActive(
                OrganizationTypeConstants.ACTIVE
        );

        entity.setIsDeleted(
                OrganizationTypeConstants.NOT_DELETED
        );

        OrganizationType saved =
                repository.save(entity);

        log.info(
                "Organization type created successfully. publicId={}",
                saved.getPublicId()
        );

        return mapper.toResponse(saved);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public OrganizationTypeResponse update(
            UpdateOrganizationTypeRequest request) {

        log.info(
                "Updating organization type. publicId={}",
                request.getPublicId()
        );

        validator.validateForUpdate(request);

        OrganizationType entity =
                validator.validateAndGet(
                        request.getPublicId()
                );

        mapper.updateEntity(
                request,
                entity
        );

        /*
         * Normalize code/name after mapping.
         */
        entity.setOrganizationTypeCode(
                request.getOrganizationTypeCode()
                        .trim()
                        .toUpperCase()
        );

        entity.setOrganizationTypeName(
                request.getOrganizationTypeName()
                        .trim()
        );

        entity.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        OrganizationType updated =
                repository.save(entity);

        log.info(
                "Organization type updated successfully. publicId={}",
                updated.getPublicId()
        );

        return mapper.toResponse(updated);
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public OrganizationTypeResponse getByPublicId(
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
    public PageResponse<OrganizationTypeResponse> getAll(
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

        Page<OrganizationType> result =
                repository.findByIsDeleted(
                        OrganizationTypeConstants.NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<OrganizationTypeResponse>builder()
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

        OrganizationType entity =
                validator.validateAndGet(publicId);

        entity.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        repository.save(entity);

        log.info(
                "Organization type deleted successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        OrganizationType entity =
                validator.validateAndGet(publicId);

        entity.activate(
                currentUserService.getCurrentUserId()
        );

        repository.save(entity);

        log.info(
                "Organization type activated successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        OrganizationType entity =
                validator.validateAndGet(publicId);

        entity.deactivate(
                currentUserService.getCurrentUserId()
        );

        repository.save(entity);

        log.info(
                "Organization type deactivated successfully. publicId={}",
                publicId
        );
    }
}