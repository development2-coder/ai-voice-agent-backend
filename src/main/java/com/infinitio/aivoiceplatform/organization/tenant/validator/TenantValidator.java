package com.infinitio.aivoiceplatform.organization.tenant.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
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
 * Handles validation rules for Tenant operations.
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

        /*
         * Validate request before accessing any fields.
         */
        if (request == null) {

            throw new BadRequestException(
                    "Tenant request cannot be null."
            );
        }

        String requestedRole = request.getRole();

        if (requestedRole != null
                && !requestedRole.isBlank()
                && !RoleConstants.ADMIN.equalsIgnoreCase(
                requestedRole.trim())) {

            throw new BadRequestException(
                    "Tenant creation supports only the ADMIN role."
            );
        }

        validatePassword(
                request
        );

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
    // CREATE PASSWORD
    // =========================================================

    private void validatePassword(
            CreateTenantRequest request) {

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new BadRequestException(
                    "Password is required."
            );
        }

        if (request.getPassword().length() < 8) {

            throw new BadRequestException(
                    "Password must contain at least 8 characters."
            );
        }

        if (request.getConfirmPassword() == null
                || request.getConfirmPassword().isBlank()) {

            throw new BadRequestException(
                    "Confirm password is required."
            );
        }

        if (!request.getPassword().equals(
                request.getConfirmPassword()
        )) {

            throw new BadRequestException(
                    "Password and confirm password do not match."
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

        if (isBlank(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "Tenant public ID is required."
            );
        }

        validateRequiredFields(
                request.getOrganizationPublicId(),
                request.getTenantCode(),
                request.getTenantName()
        );

        validateUpdatePassword(
                request
        );

        validateUpdateEmail(
                request
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
                            .equals(
                                    existingTenant.getId()
                            )) {

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
                                .equals(
                                        existingTenant.getId()
                                )) {

                            throw new ConflictException(
                                    TenantMessages
                                            .TENANT_SUBDOMAIN_EXISTS
                            );
                        }
                    });
        }
    }


    // =========================================================
    // UPDATE PASSWORD
    // =========================================================

    private void validateUpdatePassword(
            UpdateTenantRequest request) {

        boolean passwordProvided =
                request.getPassword() != null
                        && !request.getPassword().isBlank();

        boolean confirmPasswordProvided =
                request.getConfirmPassword() != null
                        && !request.getConfirmPassword().isBlank();

        /*
         * Neither password field is supplied.
         * Existing password remains unchanged.
         */
        if (!passwordProvided
                && !confirmPasswordProvided) {

            return;
        }

        /*
         * Both fields must be supplied together.
         */
        if (!passwordProvided
                || !confirmPasswordProvided) {

            throw new BadRequestException(
                    "Password and confirm password are required together."
            );
        }

        if (request.getPassword().length() < 8) {

            throw new BadRequestException(
                    "Password must contain at least 8 characters."
            );
        }

        if (!request.getPassword().equals(
                request.getConfirmPassword()
        )) {

            throw new BadRequestException(
                    "Password and confirm password do not match."
            );
        }
    }


    // =========================================================
    // UPDATE EMAIL
    // =========================================================

    private void validateUpdateEmail(
            UpdateTenantRequest request) {

        /*
         * Email is optional during update.
         */
        if (request.getEmail() == null) {
            return;
        }

        if (request.getEmail().isBlank()) {

            throw new BadRequestException(
                    "Email cannot be blank."
            );
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