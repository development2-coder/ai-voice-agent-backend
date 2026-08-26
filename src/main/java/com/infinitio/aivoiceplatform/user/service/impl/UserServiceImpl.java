package com.infinitio.aivoiceplatform.user.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.master.role.validator.RoleValidator;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.organization.validator.OrganizationValidator;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.organization.tenant.validator.TenantValidator;
import com.infinitio.aivoiceplatform.user.constant.UserConstants;
import com.infinitio.aivoiceplatform.user.constant.UserMessages;
import com.infinitio.aivoiceplatform.user.dto.request.CreateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.request.UpdateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.response.UserResponse;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.mapper.UserMapper;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import com.infinitio.aivoiceplatform.user.service.UserService;
import com.infinitio.aivoiceplatform.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * User Service Implementation.
 *
 * Handles User business operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Integer NOT_DELETED = 0;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    private final OrganizationValidator organizationValidator;

    private final RoleValidator roleValidator;

    private final TenantValidator tenantValidator;

    private final PasswordEncoder passwordEncoder;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE USER
    // =========================================================

    @Override
    public UserResponse create(
            CreateUserRequest request) {

        log.info(
                "Creating user. username={}",
                request != null
                        ? request.getUsername()
                        : null
        );

        /*
         * Basic request validation.
         */
        userValidator.validateForCreate(
                request
        );

        /*
         * Resolve required relationships.
         */
        String tenantPublicId =
                request.getTenantPublicId().trim();

        String organizationPublicId =
                request.getOrganizationPublicId().trim();

        String rolePublicId =
                request.getRolePublicId().trim();

        Tenant tenant =
                tenantValidator.validateAndGet(
                        tenantPublicId
                );

        Organization organization =
                organizationValidator.validateAndGet(
                        organizationPublicId
                );

        Role role =
                roleValidator.validateAndGet(
                        rolePublicId
                );

        /*
         * Tenant must belong to the selected organization.
         */
        validateTenantOrganization(
                tenant,
                organization
        );


        // =====================================================
        // SUPER ADMIN / FIRST USER RULES
        // =====================================================

        boolean firstUser =
                userValidator.isFirstUser();

        boolean requestedSuperAdmin =
                RoleConstants.SUPER_ADMIN.equalsIgnoreCase(
                        role.getRoleCode()
                );

        /*
         * If there are no users in the database,
         * the first user MUST be SUPER_ADMIN.
         */
        if (firstUser
                && !requestedSuperAdmin) {

            throw new BadRequestException(
                    UserMessages.FIRST_USER_MUST_BE_SUPER_ADMIN
            );
        }

        /*
         * Only one SUPER_ADMIN is allowed.
         */
        if (requestedSuperAdmin
                && userValidator.isSuperAdminAlreadyExists()) {

            throw new ConflictException(
                    UserMessages.SUPER_ADMIN_ALREADY_EXISTS
            );
        }

        /*
         * Once the first user exists, all subsequent
         * user creation must be performed by an
         * authenticated user.
         */
        if (!firstUser
                && !currentUserService.isAuthenticated()) {

            throw new BadRequestException(
                    UserMessages.AUTHENTICATION_REQUIRED
            );
        }


        // =====================================================
        // MAP USER
        // =====================================================

        User user =
                userMapper.toEntity(
                        request
                );

        user.setTenant(
                tenant
        );

        user.setOrganization(
                organization
        );

        user.setRole(
                role
        );


        // =====================================================
        // PASSWORD
        // =====================================================

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        // =====================================================
        // DERIVED FIELDS
        // =====================================================

        user.updateFullName();


        // =====================================================
        // AUDIT
        // =====================================================

        if (firstUser) {

            /*
             * No authenticated user exists when the
             * first Super Admin is created.
             *
             * 0 represents system/bootstrap.
             */
            user.setCreatedBy(
                    UserConstants.SYSTEM_USER_ID
            );

        } else {

            user.setCreatedBy(
                    currentUserService.getCurrentUserId()
            );
        }


        // =====================================================
        // SAVE
        // =====================================================

        User savedUser =
                userRepository.save(
                        user
                );

        log.info(
                "User created successfully. publicId={}",
                savedUser.getPublicId()
        );

        return userMapper.toResponse(
                savedUser
        );
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Override
    public UserResponse update(
            UpdateUserRequest request) {

        log.info(
                "Updating user. publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        userValidator.validateForUpdate(
                request
        );

        User user =
                userValidator.validateAndGet(
                        request.getPublicId().trim()
                );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        request.getTenantPublicId().trim()
                );

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId().trim()
                );

        Role role =
                roleValidator.validateAndGet(
                        request.getRolePublicId().trim()
                );

        validateTenantOrganization(
                tenant,
                organization
        );

        /*
         * Map editable User fields.
         */
        userMapper.updateEntity(
                request,
                user
        );

        user.setTenant(
                tenant
        );

        user.setOrganization(
                organization
        );

        user.setRole(
                role
        );

        /*
         * Password is optional during normal update.
         */
        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );

            user.setPasswordChangedAt(
                    LocalDateTime.now()
            );
        }

        /*
         * Rebuild derived full name.
         */
        user.updateFullName();

        /*
         * Audit information.
         */
        user.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        User updatedUser =
                userRepository.save(
                        user
                );

        log.info(
                "User updated successfully. publicId={}",
                updatedUser.getPublicId()
        );

        return userMapper.toResponse(
                updatedUser
        );
    }


    // =========================================================
    // GET USER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByPublicId(
            String publicId) {

        User user =
                userValidator.validateAndGet(
                        publicId
                );

        return userMapper.toResponse(
                user
        );
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(
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

        Page<User> result =
                userRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<UserResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(userMapper::toResponse)
                                .toList()
                )
                .totalElements(
                        result.getTotalElements()
                )
                .totalPages(
                        result.getTotalPages()
                )
                .pageNumber(
                        result.getNumber()
                )
                .pageSize(
                        result.getSize()
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
    // DELETE USER
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        User user =
                userValidator.validateAndGet(
                        publicId
                );

        user.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        userRepository.save(
                user
        );

        log.info(
                "User soft deleted successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE USER
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        User user =
                userValidator.validateAndGet(
                        publicId
                );

        user.activate(
                currentUserService.getCurrentUserId()
        );

        userRepository.save(
                user
        );

        log.info(
                "User activated successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE USER
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        User user =
                userValidator.validateAndGet(
                        publicId
                );

        user.deactivate(
                currentUserService.getCurrentUserId()
        );

        userRepository.save(
                user
        );

        log.info(
                "User deactivated successfully. publicId={}",
                publicId
        );
    }


    // =========================================================
    // TENANT / ORGANIZATION VALIDATION
    // =========================================================

    private void validateTenantOrganization(
            Tenant tenant,
            Organization organization) {

        if (tenant == null) {

            throw new BadRequestException(
                    "Tenant is required."
            );
        }

        if (organization == null) {

            throw new BadRequestException(
                    "Organization is required."
            );
        }

        if (tenant.getOrganization() == null) {

            throw new BadRequestException(
                    "Tenant is not associated with an organization."
            );
        }

        if (!tenant.getOrganization()
                .getId()
                .equals(
                        organization.getId()
                )) {

            throw new BadRequestException(
                    "Tenant does not belong to the selected organization."
            );
        }
    }
}