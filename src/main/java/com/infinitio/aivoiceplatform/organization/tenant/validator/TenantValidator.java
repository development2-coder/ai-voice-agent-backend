package com.infinitio.aivoiceplatform.organization.tenant.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.organization.tenant.constant.TenantMessages;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.CreateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.UpdateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.organization.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Tenant Validator.
 *
 * Handles Tenant request and business validations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantValidator {

    private static final Integer NOT_DELETED = 0;

    private final TenantRepository tenantRepository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateTenantRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Tenant request cannot be null."
            );
        }

        validateRequiredFields(
                request.getOrganizationPublicId(),
                request.getTenantCode(),
                request.getTenantName()
        );

        String tenantCode =
                normalize(
                        request.getTenantCode()
                );

        String subdomain =
                normalizeNullable(
                        request.getSubdomain()
                );

        /*
         * Tenant code uniqueness.
         */
        if (tenantRepository
                .existsByTenantCodeAndIsDeleted(
                        tenantCode,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    TenantMessages.TENANT_CODE_EXISTS
            );
        }

        /*
         * Subdomain uniqueness.
         */
        if (subdomain != null
                && tenantRepository
                .existsBySubdomainAndIsDeleted(
                        subdomain,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    TenantMessages.TENANT_SUBDOMAIN_EXISTS
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateTenantRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Tenant update request cannot be null."
            );
        }

        if (isBlank(request.getPublicId())) {

            throw new BadRequestException(
                    "Tenant public ID is required."
            );
        }

        validateRequiredFields(
                request.getOrganizationPublicId(),
                request.getTenantCode(),
                request.getTenantName()
        );

        String publicId =
                request.getPublicId().trim();

        Tenant existingTenant =
                validateAndGet(
                        publicId
                );

        String tenantCode =
                normalize(
                        request.getTenantCode()
                );

        String subdomain =
                normalizeNullable(
                        request.getSubdomain()
                );

        /*
         * Tenant code uniqueness.
         */
        tenantRepository
                .findByTenantCodeAndIsDeleted(
                        tenantCode,
                        NOT_DELETED
                )
                .ifPresent(existing -> {

                    if (!existing
                            .getId()
                            .equals(existingTenant.getId())) {

                        throw new ConflictException(
                                TenantMessages.TENANT_CODE_EXISTS
                        );
                    }
                });

        /*
         * Subdomain uniqueness.
         */
        if (subdomain != null) {

            tenantRepository
                    .existsBySubdomainAndIsDeleted(
                            subdomain,
                            NOT_DELETED
                    );

            /*
             * The existing repository does not provide a
             * "PublicIdNot" subdomain method.
             *
             * Therefore resolve the matching tenant and
             * compare its ID before throwing a conflict.
             */
            tenantRepository
                    .findAll()
                    .stream()
                    .filter(tenant ->
                            NOT_DELETED.equals(
                                    tenant.getIsDeleted()
                            )
                    )
                    .filter(tenant ->
                            subdomain.equalsIgnoreCase(
                                    tenant.getSubdomain()
                            )
                    )
                    .findFirst()
                    .ifPresent(existing -> {

                        if (!existing
                                .getId()
                                .equals(existingTenant.getId())) {

                            throw new ConflictException(
                                    TenantMessages
                                            .TENANT_SUBDOMAIN_EXISTS
                            );
                        }
                    });
        }
    }


    // =========================================================
// GET TENANT
// =========================================================

    public Tenant validateAndGet(
            String publicId) {

        if (isBlank(publicId)) {

            throw new BadRequestException(
                    "Tenant public ID is required."
            );
        }

        String normalizedPublicId =
                publicId.trim();

        log.info(
                "Fetching Tenant. Public Id : {}",
                normalizedPublicId
        );

        Tenant tenant =
                tenantRepository
                        .findByPublicIdAndIsDeleted(
                                normalizedPublicId,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        TenantMessages.TENANT_NOT_FOUND
                                )
                        );

        log.info(
                "Tenant found successfully. Public Id : {}, Tenant Code : {}",
                tenant.getPublicId(),
                tenant.getTenantCode()
        );

        return tenant;
    }


    // =========================================================
    // REQUIRED FIELDS
    // =========================================================

    private void validateRequiredFields(
            String organizationPublicId,
            String tenantCode,
            String tenantName) {

        if (isBlank(organizationPublicId)) {

            throw new BadRequestException(
                    "Organization is required."
            );
        }

        if (isBlank(tenantCode)) {

            throw new BadRequestException(
                    "Tenant code is required."
            );
        }

        if (isBlank(tenantName)) {

            throw new BadRequestException(
                    "Tenant name is required."
            );
        }
    }


    // =========================================================
    // NORMALIZATION
    // =========================================================

    private String normalize(
            String value) {

        return value.trim();
    }

    private String normalizeNullable(
            String value) {

        if (isBlank(value)) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}