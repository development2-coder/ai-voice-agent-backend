package com.infinitio.aivoiceplatform.user.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
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
 * <p>
 * Handles User business operations.
 * </p>
 *
 * <p>
 * User management follows the application hierarchy:
 * </p>
 *
 * <pre>
 * SUPER_ADMIN
 *     |
 *     +-- ADMIN
 *             |
 *             +-- USER
 * </pre>
 *
 * <p>
 * SUPER_ADMIN has global access.
 * ADMIN can manage only USER records belonging to its own tenant.
 * USER cannot perform user-management operations.
 * </p>
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

        /*
         * First-user/bootstrap rules.
         */
        boolean firstUser =
                userValidator.isFirstUser();

        boolean requestedSuperAdmin =
                isSuperAdminRole(role);

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
         * Once the first user exists, authentication
         * is required for all further user creation.
         */
        if (!firstUser
                && !currentUserService.isAuthenticated()) {

            throw new BadRequestException(
                    UserMessages.AUTHENTICATION_REQUIRED
            );
        }

        /*
         * RBAC + tenant validation.
         *
         * SUPER_ADMIN:
         *      Can create ADMIN and USER.
         *
         * ADMIN:
         *      Can create USER only and only inside
         *      the current tenant.
         *
         * USER:
         *      Cannot create users.
         */
        if (!firstUser) {

            validateCreateAccess(
                    role,
                    tenant,
                    organization
            );
        }

        /*
         * MAP USER
         */
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

        /*
         * PASSWORD
         */
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        /*
         * DERIVED FIELDS
         */
        user.updateFullName();

        /*
         * AUDIT
         */
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

        /*
         * SAVE
         */
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
    public UserResponse update(UpdateUserRequest request) {

        log.info(
                "Updating user. publicId={}",
                request != null ? request.getPublicId() : null
        );

        userValidator.validateForUpdate(request);

        /*
         * Load the target within the caller's authorized scope.
         * SUPER_ADMIN searches globally; ADMIN searches its tenant.
         */
        User user = getUserForManagement(
                request.getPublicId().trim()
        );

        Tenant tenant = tenantValidator.validateAndGet(
                request.getTenantPublicId().trim()
        );

        Organization organization =
                organizationValidator.validateAndGet(
                        request.getOrganizationPublicId().trim()
                );

        Role role = roleValidator.validateAndGet(
                request.getRolePublicId().trim()
        );

        validateTenantOrganization(tenant, organization);

        /*
         * Validate both the existing target and the requested
         * destination before changing any entity fields.
         */
        validateUpdateAccess(
                user,
                tenant,
                organization,
                role
        );

        userMapper.updateEntity(request, user);

        user.setTenant(tenant);
        user.setOrganization(organization);
        user.setRole(role);

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );

            user.setPasswordChangedAt(LocalDateTime.now());
        }

        user.updateFullName();

        user.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        User updatedUser = userRepository.save(user);

        log.info(
                "User updated successfully. publicId={}",
                updatedUser.getPublicId()
        );

        return userMapper.toResponse(updatedUser);
    }


    // =========================================================
    // GET USER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByPublicId(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    "User public ID is required."
            );
        }

        User user =
                getUserForCurrentUser(
                        publicId.trim()
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

        /*
         * USER should not access user-management listing.
         */
        validateUserManagementAccess();

        Page<User> result;

        /*
         * SUPER_ADMIN can see users across all tenants.
         */
        if (currentUserService.isSuperAdmin()) {

            result =
                    userRepository.findByIsDeleted(
                            NOT_DELETED,
                            PageRequest.of(
                                    page,
                                    size
                            )
                    );

        } else {

            /*
             * ADMIN can see users only from its own tenant.
             */
            Long tenantId =
                    currentUserService.getCurrentTenantId();

            result =
                    userRepository.findByTenant_IdAndIsDeleted(
                            tenantId,
                            NOT_DELETED,
                            PageRequest.of(
                                    page,
                                    size
                            )
                    );
        }

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
                getUserForManagement(
                        publicId
                );

        validateTargetUserManagementAccess(
                user
        );

        /*
         * Prevent deleting yourself through the user-management
         * endpoint.
         */
        if (user.getId().equals(
                currentUserService.getCurrentUserId()
        )) {

            throw new BadRequestException(
                    "You cannot delete your own user account."
            );
        }

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
                getUserForManagement(
                        publicId
                );

        validateTargetUserManagementAccess(
                user
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
                getUserForManagement(
                        publicId
                );

        validateTargetUserManagementAccess(
                user
        );

        /*
         * Prevent deactivating yourself.
         */
        if (user.getId().equals(
                currentUserService.getCurrentUserId()
        )) {

            throw new BadRequestException(
                    "You cannot deactivate your own user account."
            );
        }

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
    // CREATE ACCESS
    // =========================================================

    /**
     * Validates whether the current authenticated user can
     * create the requested user.
     *
     * <p>
     * SUPER_ADMIN can create ADMIN and USER.
     * ADMIN can create USER only inside its own tenant.
     * USER cannot create users.
     * </p>
     */
    private void validateCreateAccess(
            Role requestedRole,
            Tenant tenant,
            Organization organization) {

        String currentRole =
                currentUserService.getCurrentRoleCode();

        /*
         * SUPER_ADMIN has global access.
         */
        if (RoleConstants.SUPER_ADMIN.equalsIgnoreCase(
                currentRole
        )) {

            if (!RoleConstants.ADMIN.equalsIgnoreCase(
                    requestedRole.getRoleCode()
            )
                    && !RoleConstants.USER.equalsIgnoreCase(
                    requestedRole.getRoleCode()
            )) {

                throw new ForbiddenException(
                        "SUPER_ADMIN can create only ADMIN or USER accounts."
                );
            }

            return;
        }

        /*
         * ADMIN can create USER only.
         */
        if (RoleConstants.ADMIN.equalsIgnoreCase(
                currentRole
        )) {

            if (!RoleConstants.USER.equalsIgnoreCase(
                    requestedRole.getRoleCode()
            )) {

                throw new ForbiddenException(
                        "Admin can create only USER accounts."
                );
            }

            validateCurrentAdminTenant(
                    tenant,
                    organization
            );

            return;
        }

        /*
         * USER has no user-management creation access.
         */
        throw new ForbiddenException(
                "User does not have permission to create users."
        );
    }


    // =========================================================
    // UPDATE ACCESS
    // =========================================================

    /**
     * Validates whether the current authenticated user can
     * update the supplied target user.
     */
    private void validateUpdateAccess(
            User targetUser,
            Tenant requestedTenant,
            Organization requestedOrganization,
            Role requestedRole) {

        validateUserManagementAccess();

        /*
         * SUPER_ADMIN can manage users globally.
         */
        if (currentUserService.isSuperAdmin()) {
            return;
        }

        /*
         * From this point only ADMIN is allowed.
         */
        if (!currentUserService.isAdmin()) {

            throw new ForbiddenException(
                    "User does not have permission to update users."
            );
        }

        /*
         * ADMIN can manage USER accounts only.
         */
        if (!isUserRole(targetUser.getRole())) {

            throw new ForbiddenException(
                    "Admin can manage only USER accounts."
            );
        }

        /*
         * ADMIN can assign USER role only.
         */
        if (!isUserRole(requestedRole)) {

            throw new ForbiddenException(
                    "Admin can assign only the USER role."
            );
        }

        /*
         * Target user must belong to current tenant.
         */
        validateCurrentAdminTenant(
                targetUser.getTenant(),
                targetUser.getOrganization()
        );

        /*
         * Requested tenant must remain the current admin's tenant.
         */
        validateCurrentAdminTenant(
                requestedTenant,
                requestedOrganization
        );
    }


    // =========================================================
    // TARGET USER MANAGEMENT ACCESS
    // =========================================================

    /**
     * Validates access for delete, activate and deactivate
     * operations.
     */
    private void validateTargetUserManagementAccess(
            User targetUser) {

        validateUserManagementAccess();

        /*
         * SUPER_ADMIN has global access.
         */
        if (currentUserService.isSuperAdmin()) {
            return;
        }

        /*
         * ADMIN can manage USER accounts only.
         */
        if (currentUserService.isAdmin()) {

            if (!isUserRole(targetUser.getRole())) {

                throw new ForbiddenException(
                        "Admin can manage only USER accounts."
                );
            }

            validateCurrentAdminTenant(
                    targetUser.getTenant(),
                    targetUser.getOrganization()
            );

            return;
        }

        throw new ForbiddenException(
                "User does not have permission to manage users."
        );
    }


    // =========================================================
    // GET USER ACCESS
    // =========================================================

    /**
     * Returns a user according to the current user's scope.
     *
     * <p>
     * SUPER_ADMIN searches globally.
     * ADMIN searches inside the current tenant.
     * USER cannot use user-management access to retrieve
     * another user.
     * </p>
     */
    private User getUserForCurrentUser(
            String publicId) {

        /*
         * SUPER_ADMIN can access users from every tenant.
         */
        if (currentUserService.isSuperAdmin()) {

            return userValidator.validateAndGet(
                    publicId
            );
        }

        /*
         * ADMIN can access users only inside its tenant.
         */
        if (currentUserService.isAdmin()) {

            Long tenantId =
                    currentUserService.getCurrentTenantId();

            return userRepository
                    .findByTenant_IdAndPublicIdAndIsDeleted(
                            tenantId,
                            publicId,
                            NOT_DELETED
                    )
                    .orElseThrow(() ->
                            new com.infinitio.aivoiceplatform.exception.ResourceNotFoundException(
                                    "User not found."
                            )
                    );
        }

        /*
         * USER should not use this endpoint for
         * arbitrary user access.
         */
        throw new ForbiddenException(
                "User does not have permission to access user details."
        );
    }


    // =========================================================
    // GET USER FOR MANAGEMENT
    // =========================================================

    /**
     * Loads a user for management operations.
     *
     * <p>
     * The actual authorization is performed separately by
     * validateTargetUserManagementAccess().
     * </p>
     */
    private User getUserForManagement(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    "User public ID is required."
            );
        }

        /*
         * SUPER_ADMIN can search globally.
         */
        if (currentUserService.isSuperAdmin()) {

            return userValidator.validateAndGet(
                    publicId.trim()
            );
        }

        /*
         * ADMIN must search inside its own tenant.
         *
         * This is important because loading the user globally
         * first would allow cross-tenant access.
         */
        if (currentUserService.isAdmin()) {

            Long tenantId =
                    currentUserService.getCurrentTenantId();

            return userRepository
                    .findByTenant_IdAndPublicIdAndIsDeleted(
                            tenantId,
                            publicId.trim(),
                            NOT_DELETED
                    )
                    .orElseThrow(() ->
                            new com.infinitio.aivoiceplatform.exception.ResourceNotFoundException(
                                    "User not found."
                            )
                    );
        }

        throw new ForbiddenException(
                "User does not have permission to manage users."
        );
    }


    // =========================================================
    // USER MANAGEMENT ACCESS
    // =========================================================

    /**
     * Validates whether the current user can use
     * user-management operations.
     */
    private void validateUserManagementAccess() {

        if (!currentUserService.isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required for user management."
            );
        }

        if (currentUserService.isSuperAdmin()
                || currentUserService.isAdmin()) {

            return;
        }

        throw new ForbiddenException(
                "User does not have permission to manage users."
        );
    }


    // =========================================================
    // ADMIN TENANT VALIDATION
    // =========================================================

    /**
     * Ensures that an ADMIN operates only inside its own
     * tenant and organization.
     */
    private void validateCurrentAdminTenant(
            Tenant tenant,
            Organization organization) {

        if (!currentUserService.isAdmin()) {
            return;
        }

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUser.getTenant() == null) {

            throw new BadRequestException(
                    "Current Admin is not associated with a Tenant."
            );
        }

        if (tenant == null) {

            throw new BadRequestException(
                    "Tenant is required."
            );
        }

        if (!currentUser.getTenant()
                .getId()
                .equals(tenant.getId())) {

            throw new ForbiddenException(
                    "Admin cannot access another Tenant."
            );
        }

        if (currentUser.getOrganization() == null) {

            throw new BadRequestException(
                    "Current Admin is not associated with an Organization."
            );
        }

        if (organization == null) {

            throw new BadRequestException(
                    "Organization is required."
            );
        }

        if (!currentUser.getOrganization()
                .getId()
                .equals(organization.getId())) {

            throw new ForbiddenException(
                    "Admin cannot access another Organization."
            );
        }
    }


    // =========================================================
    // ROLE HELPERS
    // =========================================================

    /**
     * Checks whether a role is SUPER_ADMIN.
     */
    private boolean isSuperAdminRole(
            Role role) {

        return role != null
                && RoleConstants.SUPER_ADMIN.equalsIgnoreCase(
                role.getRoleCode()
        );
    }


    /**
     * Checks whether a role is USER.
     */
    private boolean isUserRole(
            Role role) {

        return role != null
                && RoleConstants.USER.equalsIgnoreCase(
                role.getRoleCode()
        );
    }


    // =========================================================
    // TENANT / ORGANIZATION VALIDATION
    // =========================================================

    /**
     * Validates that a tenant belongs to the selected
     * organization.
     */
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