package com.infinitio.aivoiceplatform.organization.tenant.service.impl;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.validator.RoleValidator;
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
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    private static final Integer NOT_DELETED = 0;

    private static final Long SYSTEM_USER_ID = 1L;

    private final TenantRepository tenantRepository;

    private final TenantMapper tenantMapper;

    private final TenantValidator tenantValidator;

    private final OrganizationValidator organizationValidator;

    private final RoleValidator roleValidator;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


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
         * New Tenant is active by default.
         */
        tenant.setIsActive(
                ACTIVE
        );

        /*
         * Default Tenant flag.
         */
        if (tenant.getIsDefault() == null) {

            tenant.setIsDefault(
                    Boolean.FALSE
            );
        }

        /*
         * Audit information.
         */
        tenant.setCreatedBy(
                SYSTEM_USER_ID
        );

        Tenant savedTenant =
                tenantRepository.save(
                        tenant
                );

        /*
         * Create the login user for the Tenant.
         *
         * The Tenant uses the existing ADMIN role.
         * The email and password entered during Tenant
         * creation are used for login.
         */
        createTenantAdminUser(
                request,
                savedTenant,
                organization
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
    // CREATE TENANT ADMIN USER
    // =========================================================

    private void createTenantAdminUser(
            CreateTenantRequest request,
            Tenant tenant,
            Organization organization) {

        String email =
                request.getEmail().trim();

        /*
         * Prevent duplicate login credentials.
         */
        if (userRepository.existsByEmailAndIsDeleted(
                email,
                NOT_DELETED
        )) {

            throw new ConflictException(
                    "A user already exists with this email."
            );
        }

        /*
         * Get existing ADMIN role.
         */
        String requestedRole = request.getRole();

        String roleCode =
                requestedRole == null || requestedRole.isBlank()
                        ? RoleConstants.ADMIN
                        : requestedRole.trim().toUpperCase(
                        java.util.Locale.ROOT
                );

        if (!RoleConstants.ADMIN.equals(roleCode)) {
            throw new IllegalArgumentException(
                    "Tenant creation supports only the ADMIN role."
            );
        }

        Role adminRole =
                roleValidator.validateAndGetByRoleCode(roleCode);

        /*
         * Create Tenant Admin User.
         */
        User tenantAdmin =
                User.builder()
                        .tenant(tenant)
                        .organization(organization)
                        .role(adminRole)

                        .username(email)
                        .email(email)

                        /*
                         * Never store the plain-text password.
                         */
                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )

                        .firstName(
                                request.getTenantName()
                        )
                        .lastName("Admin")

                        .mobileNumber(
                                request.getPhoneNumber()
                        )

                        .isActive(ACTIVE)
                        .isDeleted(NOT_DELETED)

                        .passwordChangedAt(
                                LocalDateTime.now()
                        )

                        .createdBy(
                                SYSTEM_USER_ID
                        )
                        .build();

        tenantAdmin.updateFullName();

        userRepository.save(
                tenantAdmin
        );

        log.info(
                "Tenant Admin user created successfully. email={}, tenantPublicId={}",
                email,
                tenant.getPublicId()
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

        /*
         * Store the current Tenant Admin email before
         * updating the Tenant email.
         *
         * This is required because the User lookup
         * must use the existing email.
         */
        String existingTenantEmail =
                existingTenant.getEmail();

        /*
         * Validate a new email before modifying
         * the Tenant.
         */
        validateTenantAdminEmailChange(
                existingTenant,
                request
        );

        /*
         * Update normal Tenant fields.
         *
         * Password and confirmPassword are not mapped
         * to Tenant because they belong to the User.
         */
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

        /*
         * Synchronize Tenant Admin login credentials.
         */
        updateTenantAdminUser(
                existingTenant,
                existingTenantEmail,
                request
        );

        Tenant updatedTenant =
                tenantRepository.save(
                        existingTenant
                );

        log.info(
                "Tenant updated successfully. Public Id : {}",
                updatedTenant.getPublicId()
        );

        TenantResponse response =
                tenantMapper.toResponse(
                        updatedTenant
                );

        response.setPassword(
                request.getPassword()
        );

        return response;
    }


    // =========================================================
    // VALIDATE ADMIN EMAIL CHANGE
    // =========================================================

    private void validateTenantAdminEmailChange(
            Tenant existingTenant,
            UpdateTenantRequest request) {

        /*
         * Email was not supplied.
         */
        if (request.getEmail() == null) {
            return;
        }

        String newEmail =
                request.getEmail().trim();

        String existingEmail =
                existingTenant.getEmail();

        /*
         * Email did not change.
         */
        if (existingEmail != null
                && existingEmail.equalsIgnoreCase(
                newEmail
        )) {

            return;
        }

        /*
         * New email cannot belong to another User.
         */
        if (userRepository.existsByEmailAndIsDeleted(
                newEmail,
                NOT_DELETED
        )) {

            throw new ConflictException(
                    "A user already exists with this email."
            );
        }
    }


    // =========================================================
    // UPDATE TENANT ADMIN USER
    // =========================================================

    private void updateTenantAdminUser(
            Tenant tenant,
            String existingTenantEmail,
            UpdateTenantRequest request) {

        /*
         * Find the automatically-created ADMIN user
         * belonging to this Tenant.
         */
        User tenantAdmin =
                userRepository
                        .findByTenant_IdAndEmailAndIsDeleted(
                                tenant.getId(),
                                existingTenantEmail,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Tenant Admin user not found."
                                )
                        );

        /*
         * Synchronize login email.
         */
        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {

            String newEmail =
                    request.getEmail().trim();

            tenantAdmin.setEmail(
                    newEmail
            );

            tenantAdmin.setUsername(
                    newEmail
            );
        }

        /*
         * Update password only when supplied.
         *
         * If password and confirmPassword are null,
         * the existing password remains unchanged.
         */
        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            tenantAdmin.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );

            tenantAdmin.setPasswordChangedAt(
                    LocalDateTime.now()
            );
        }

        tenantAdmin.setUpdatedBy(
                SYSTEM_USER_ID
        );

        userRepository.save(
                tenantAdmin
        );

        log.info(
                "Tenant Admin user updated successfully. tenantPublicId={}",
                tenant.getPublicId()
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