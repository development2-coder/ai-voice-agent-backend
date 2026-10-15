package com.infinitio.aivoiceplatform.auth.service;

import com.infinitio.aivoiceplatform.exception.UnauthorizedException;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.ACTIVE;
import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Provides information about the currently authenticated user.
 *
 * <p>This service is the central source for authenticated-user,
 * role, tenant and organization information within the application.</p>
 *
 * <p>Tenant-aware services should use this service to determine the
 * current tenant instead of trusting tenant identifiers supplied
 * by client requests.</p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

//    private static final Integer NOT_DELETED = 0;
//    private static final Integer ACTIVE = 1;

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    private final UserRepository userRepository;

    // =========================================================
    // AUTHENTICATION CHECK
    // =========================================================

    /**
     * Checks whether a user is currently authenticated.
     *
     * @return true when an authenticated non-anonymous user exists
     */
    public boolean isAuthenticated() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        if (!authentication.isAuthenticated()) {
            return false;
        }

        if ("anonymousUser".equals(
                authentication.getPrincipal())) {

            return false;
        }

        return authentication.getPrincipal() != null;
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    /**
     * Returns the currently authenticated application user.
     *
     * @return current authenticated user
     * @throws UnauthorizedException when the user is not authenticated
     */
    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(
                authentication.getPrincipal())) {

            throw new UnauthorizedException(
                    "User is not authenticated."
            );
        }

        String email =
                authentication.getName();

        if (email == null
                || email.isBlank()) {

            throw new UnauthorizedException(
                    "Authenticated user information is unavailable."
            );
        }

        return userRepository
                .findByEmailAndIsDeleted(
                        email,
                        NOT_DELETED
                )
                .filter(user ->
                        ACTIVE.equals(
                                user.getIsActive()
                        )
                )
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Authenticated user is not available."
                        )
                );
    }

    // =========================================================
    // CURRENT USER IDENTIFIERS
    // =========================================================

    /**
     * Returns the current user's database ID.
     *
     * @return current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Returns the current user's public ID.
     *
     * @return current user public ID
     */
    public String getCurrentUserPublicId() {
        return getCurrentUser().getPublicId();
    }

    /**
     * Returns the current user's email.
     *
     * @return current user email
     */
    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    // =========================================================
    // CURRENT ROLE
    // =========================================================

    /**
     * Returns the current user's role code.
     *
     * @return role code
     */
    public String getCurrentRoleCode() {
        User user = getCurrentUser();

        if (user.getRole() == null
                || user.getRole().getRoleCode() == null) {
            throw new UnauthorizedException(
                    "Authenticated user role is unavailable."
            );
        }

        return user.getRole().getRoleCode();
    }

    /**
     * Checks whether the current user is a super administrator.
     *
     * @return true when current user has SUPER_ADMIN role
     */
    public boolean isSuperAdmin() {
        return SUPER_ADMIN_ROLE.equalsIgnoreCase(
                getCurrentRoleCode()
        );
    }

    /**
     * Checks whether the current user is an administrator.
     *
     * @return true when current user has ADMIN role
     */
    public boolean isAdmin() {
        return ADMIN_ROLE.equalsIgnoreCase(
                getCurrentRoleCode()
        );
    }

    /**
     * Checks whether the current user is a normal user.
     *
     * @return true when current user has USER role
     */
    public boolean isUser() {
        return USER_ROLE.equalsIgnoreCase(
                getCurrentRoleCode()
        );
    }

    // =========================================================
    // CURRENT TENANT
    // =========================================================

    /**
     * Returns the tenant ID associated with the current user.
     *
     * <p>SUPER_ADMIN may not have a tenant because it represents
     * the company/application owner. Therefore this method returns
     * null for SUPER_ADMIN.</p>
     *
     * @return current tenant ID, or null for SUPER_ADMIN
     */
    public Long getCurrentTenantId() {

        User user = getCurrentUser();

        if (isSuperAdmin()) {
            return null;
        }

        if (user.getTenant() == null
                || user.getTenant().getId() == null) {

            throw new UnauthorizedException(
                    "Authenticated user tenant is unavailable."
            );
        }

        return user.getTenant().getId();
    }

    /**
     * Returns the tenant associated with the current user.
     *
     * <p>SUPER_ADMIN does not belong to a specific tenant, so this
     * method returns null for SUPER_ADMIN.</p>
     *
     * @return current tenant, or null for SUPER_ADMIN
     */
    /**
     * Returns the tenant associated with the current user.
     *
     * <p>SUPER_ADMIN does not belong to a specific tenant, so this
     * method returns null for SUPER_ADMIN.</p>
     *
     * @return current tenant, or null for SUPER_ADMIN
     */
    public Tenant getCurrentTenant() {

        User user = getCurrentUser();

        if (isSuperAdmin()) {
            return null;
        }

        if (user.getTenant() == null) {
            throw new UnauthorizedException(
                    "Authenticated user tenant is unavailable."
            );
        }

        return user.getTenant();
    }

    /**
     * Returns the public ID of the current tenant.
     *
     * @return current tenant public ID, or null for SUPER_ADMIN
     */
    /**
     * Returns the public ID of the current tenant.
     *
     * @return current tenant public ID, or null for SUPER_ADMIN
     */
    public String getCurrentTenantPublicId() {

        com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant tenant =
                getCurrentTenant();

        if (tenant == null) {
            return null;
        }

        return tenant.getPublicId();
    }

    // =========================================================
    // CURRENT ORGANIZATION
    // =========================================================

    /**
     * Returns the organization ID associated with the current user.
     *
     * @return organization ID, or null when organization is unavailable
     */
    public Long getCurrentOrganizationId() {

        User user = getCurrentUser();

        if (user.getOrganization() == null) {
            return null;
        }

        return user.getOrganization().getId();
    }

    /**
     * Returns whether the current user belongs to the supplied tenant.
     *
     * <p>SUPER_ADMIN has global access and therefore returns true.</p>
     *
     * @param tenantId tenant ID
     * @return true when the current user can access the tenant
     */
    public boolean belongsToTenant(Long tenantId) {

        if (tenantId == null) {
            return false;
        }

        if (isSuperAdmin()) {
            return true;
        }

        Long currentTenantId =
                getCurrentTenantId();

        return tenantId.equals(currentTenantId);
    }
}