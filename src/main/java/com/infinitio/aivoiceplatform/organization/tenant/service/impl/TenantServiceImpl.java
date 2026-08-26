package com.infinitio.aivoiceplatform.organization.tenant.service.impl;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.organization.validator.OrganizationValidator;
import com.infinitio.aivoiceplatform.organization.tenant.constant.TenantMessages;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.CreateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.UpdateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.response.TenantResponse;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.organization.tenant.mapper.TenantMapper;
import com.infinitio.aivoiceplatform.organization.tenant.repository.TenantRepository;
import com.infinitio.aivoiceplatform.organization.tenant.service.TenantService;
import com.infinitio.aivoiceplatform.organization.tenant.specification.TenantSpecification;
import com.infinitio.aivoiceplatform.organization.tenant.validator.TenantValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tenant Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TenantServiceImpl
        implements TenantService {

    private static final Integer ACTIVE = 1;
    private static final Integer INACTIVE = 0;

    private static final Integer NOT_DELETED = 0;

    private static final Long SYSTEM_USER_ID = 1L;

    private final TenantRepository tenantRepository;

    private final TenantMapper tenantMapper;

    private final TenantValidator tenantValidator;

    private final OrganizationValidator organizationValidator;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public TenantResponse create(
            CreateTenantRequest request) {

        log.info(
                "Creating Tenant. Tenant Code : {}",
                request != null
                        ? request.getTenantCode()
                        : null
        );

        tenantValidator.validateForCreate(
                request
        );

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId()
                );

        Tenant tenant =
                tenantMapper.toEntity(
                        request
                );

        tenant.setOrganization(
                organization
        );

        /*
         * New tenant is active by default.
         */
        tenant.setIsActive(
                ACTIVE
        );

        /*
         * Default tenant flag.
         */
        if (tenant.getIsDefault() == null) {

            tenant.setIsDefault(
                    Boolean.FALSE
            );
        }

        /*
         * Audit information.
         *
         * Replace SYSTEM_USER_ID later with the
         * authenticated user context when that
         * functionality is centralized.
         */
        tenant.setCreatedBy(
                SYSTEM_USER_ID
        );

        Tenant savedTenant =
                tenantRepository.save(
                        tenant
                );

        log.info(
                "Tenant created successfully. Public Id : {}",
                savedTenant.getPublicId()
        );

        return tenantMapper.toResponse(
                savedTenant
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public TenantResponse update(
            UpdateTenantRequest request) {

        log.info(
                "Updating Tenant. Public Id : {}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        tenantValidator.validateForUpdate(
                request
        );

        Tenant existingTenant =
                tenantValidator.validateAndGet(
                        request.getPublicId()
                );

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId()
                );

        tenantMapper.updateEntity(
                request,
                existingTenant
        );

        existingTenant.setOrganization(
                organization
        );

        existingTenant.setUpdatedBy(
                SYSTEM_USER_ID
        );

        Tenant updatedTenant =
                tenantRepository.save(
                        existingTenant
                );

        log.info(
                "Tenant updated successfully. Public Id : {}",
                updatedTenant.getPublicId()
        );

        return tenantMapper.toResponse(
                updatedTenant
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Tenant. Public Id : {}",
                publicId
        );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        publicId
                );

        return tenantMapper.toResponse(
                tenant
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TenantResponse> getAll(
            int page,
            int size) {

        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page cannot be negative."
            );
        }

        if (size <= 0) {

            throw new IllegalArgumentException(
                    "Size must be greater than zero."
            );
        }

        log.info(
                "Fetching Tenants. Page : {}, Size : {}",
                page,
                size
        );

        Page<Tenant> result =
                tenantRepository.findAll(
                        TenantSpecification.isNotDeleted(),
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse.<TenantResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        tenantMapper::toResponse
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
                "Deleting Tenant. Public Id : {}",
                publicId
        );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        publicId
                );

        tenant.markAsDeleted(
                SYSTEM_USER_ID
        );

        tenantRepository.save(
                tenant
        );

        log.info(
                "Tenant deleted successfully. Public Id : {}",
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
                "Activating Tenant. Public Id : {}",
                publicId
        );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        publicId
                );

        tenant.activate(
                SYSTEM_USER_ID
        );

        tenantRepository.save(
                tenant
        );

        log.info(
                "Tenant activated successfully. Public Id : {}",
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
                "Deactivating Tenant. Public Id : {}",
                publicId
        );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        publicId
                );

        tenant.deactivate(
                SYSTEM_USER_ID
        );

        tenantRepository.save(
                tenant
        );

        log.info(
                "Tenant deactivated successfully. Public Id : {}",
                publicId
        );
    }
}