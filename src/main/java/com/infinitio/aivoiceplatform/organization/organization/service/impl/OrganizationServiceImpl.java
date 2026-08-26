package com.infinitio.aivoiceplatform.organization.organization.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.organization.organization.constant.OrganizationConstants;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.CreateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.UpdateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.response.OrganizationResponse;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.organization.mapper.OrganizationMapper;
import com.infinitio.aivoiceplatform.organization.organization.repository.OrganizationRepository;
import com.infinitio.aivoiceplatform.organization.organization.service.OrganizationService;
import com.infinitio.aivoiceplatform.organization.organization.validator.OrganizationValidator;
import com.infinitio.aivoiceplatform.organization.organizationstatus.entity.OrganizationStatus;
import com.infinitio.aivoiceplatform.organization.organizationstatus.validator.OrganizationStatusValidator;
import com.infinitio.aivoiceplatform.organization.organizationtype.entity.OrganizationType;
import com.infinitio.aivoiceplatform.organization.organizationtype.validator.OrganizationTypeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Organization Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationServiceImpl
        implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final OrganizationMapper organizationMapper;

    private final OrganizationValidator organizationValidator;

    private final OrganizationTypeValidator organizationTypeValidator;

    private final OrganizationStatusValidator organizationStatusValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public OrganizationResponse create(
            CreateOrganizationRequest request) {

        log.info(
                "Creating organization. code={}",
                request.getOrganizationCode()
        );

        organizationValidator.validateForCreate(request);

        OrganizationType organizationType =
                organizationTypeValidator.validateAndGet(
                        request.getOrganizationTypePublicId()
                );

        OrganizationStatus organizationStatus =
                organizationStatusValidator.validateAndGet(
                        request.getOrganizationStatusPublicId()
                );

        Organization organization =
                organizationMapper.toEntity(request);

        organization.setOrganizationType(
                organizationType
        );

        organization.setOrganizationStatus(
                organizationStatus
        );

        organization.setOrganizationCode(
                request.getOrganizationCode()
                        .trim()
                        .toUpperCase()
        );

        organization.setOrganizationName(
                request.getOrganizationName()
                        .trim()
        );

        if (request.getEmail() != null) {
            organization.setEmail(
                    request.getEmail()
                            .trim()
                            .toLowerCase()
            );
        }

        organization.setIsActive(
                OrganizationConstants.ACTIVE
        );

        organization.setIsDeleted(
                OrganizationConstants.NOT_DELETED
        );

        organization.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        Organization saved =
                organizationRepository.save(
                        organization
                );

        log.info(
                "Organization created successfully. publicId={}",
                saved.getPublicId()
        );

        return organizationMapper.toResponse(saved);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public OrganizationResponse update(
            UpdateOrganizationRequest request) {

        log.info(
                "Updating organization. publicId={}",
                request.getPublicId()
        );

        organizationValidator.validateForUpdate(
                request
        );

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getPublicId()
                );

        OrganizationType organizationType =
                organizationTypeValidator.validateAndGet(
                        request.getOrganizationTypePublicId()
                );

        OrganizationStatus organizationStatus =
                organizationStatusValidator.validateAndGet(
                        request.getOrganizationStatusPublicId()
                );

        organizationMapper.updateEntity(
                request,
                organization
        );

        organization.setOrganizationType(
                organizationType
        );

        organization.setOrganizationStatus(
                organizationStatus
        );

        organization.setOrganizationCode(
                request.getOrganizationCode()
                        .trim()
                        .toUpperCase()
        );

        organization.setOrganizationName(
                request.getOrganizationName()
                        .trim()
        );

        if (request.getEmail() != null) {
            organization.setEmail(
                    request.getEmail()
                            .trim()
                            .toLowerCase()
            );
        }

        organization.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        Organization updated =
                organizationRepository.save(
                        organization
                );

        log.info(
                "Organization updated successfully. publicId={}",
                updated.getPublicId()
        );

        return organizationMapper.toResponse(updated);
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getByPublicId(
            String publicId) {

        Organization organization =
                organizationValidator.validateAndGet(
                        publicId
                );

        return organizationMapper.toResponse(
                organization
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationResponse> getAll(
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

        Page<Organization> result =
                organizationRepository.findByIsDeleted(
                        OrganizationConstants.NOT_DELETED,
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<OrganizationResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        organizationMapper::toResponse
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

        Organization organization =
                organizationValidator.validateAndGet(
                        publicId
                );

        organization.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        organizationRepository.save(
                organization
        );

        log.info(
                "Organization deleted successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        Organization organization =
                organizationValidator.validateAndGet(
                        publicId
                );

        organization.activate(
                currentUserService.getCurrentUserId()
        );

        organizationRepository.save(
                organization
        );

        log.info(
                "Organization activated successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        Organization organization =
                organizationValidator.validateAndGet(
                        publicId
                );

        organization.deactivate(
                currentUserService.getCurrentUserId()
        );

        organizationRepository.save(
                organization
        );

        log.info(
                "Organization deactivated successfully. publicId={}",
                publicId
        );
    }
}