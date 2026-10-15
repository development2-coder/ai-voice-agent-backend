package com.infinitio.aivoiceplatform.user.repository;

import com.infinitio.aivoiceplatform.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository.
 *
 * <p>
 * Handles database operations for User.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    /**
     * Find a non-deleted user by public ID.
     *
     * @param publicId user public ID
     * @param isDeleted deleted flag
     * @return user if found
     */
    Optional<User> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY USERNAME
    // =========================================================

    /**
     * Find a non-deleted user by username.
     *
     * @param username username
     * @param isDeleted deleted flag
     * @return user if found
     */
    Optional<User> findByUsernameAndIsDeleted(
            String username,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY EMAIL
    // =========================================================

    /**
     * Find a non-deleted user by email.
     *
     * <p>
     * Role is fetched together with User because Spring Security
     * requires role authorities during JWT authentication.
     * </p>
     *
     * @param email email
     * @param isDeleted deleted flag
     * @return user if found
     */
    @EntityGraph(attributePaths = {
            "role"
    })
    Optional<User> findByEmailAndIsDeleted(
            String email,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - ANY NON-DELETED USER
    // =========================================================

    /**
     * Check whether at least one non-deleted user exists.
     *
     * <p>
     * Used for first-user creation logic.
     * </p>
     *
     * @param isDeleted deleted flag
     * @return true if at least one user exists
     */
    boolean existsByIsDeleted(
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - USERNAME
    // =========================================================

    /**
     * Check whether a non-deleted user exists
     * with the supplied username.
     *
     * @param username username
     * @param isDeleted deleted flag
     * @return true if username exists
     */
    boolean existsByUsernameAndIsDeleted(
            String username,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - EMAIL
    // =========================================================

    /**
     * Check whether a non-deleted user exists
     * with the supplied email.
     *
     * @param email email
     * @param isDeleted deleted flag
     * @return true if email exists
     */
    boolean existsByEmailAndIsDeleted(
            String email,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - ROLE
    // =========================================================

    /**
     * Check whether a non-deleted user exists
     * with the supplied role code.
     *
     * <p>
     * Example:
     * SUPER_ADMIN
     * </p>
     *
     * @param roleCode role code
     * @param isDeleted deleted flag
     * @return true if user exists with role
     */
    boolean existsByRole_RoleCodeAndIsDeleted(
            String roleCode,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - USERNAME EXCLUDING CURRENT USER
    // =========================================================

    /**
     * Check whether another non-deleted user exists
     * with the supplied username.
     *
     * <p>
     * Used during user update validation.
     * </p>
     *
     * @param username username
     * @param isDeleted deleted flag
     * @param publicId current user's public ID
     * @return true if another user exists
     */
    boolean existsByUsernameAndIsDeletedAndPublicIdNot(
            String username,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // EXISTS - EMAIL EXCLUDING CURRENT USER
    // =========================================================

    /**
     * Check whether another non-deleted user exists
     * with the supplied email.
     *
     * <p>
     * Used during user update validation.
     * </p>
     *
     * @param email email
     * @param isDeleted deleted flag
     * @param publicId current user's public ID
     * @return true if another user exists
     */
    boolean existsByEmailAndIsDeletedAndPublicIdNot(
            String email,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // PAGINATION - NON-DELETED USERS
    // =========================================================

    /**
     * Find all non-deleted users using pagination.
     *
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated users
     */
    Page<User> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );


    // =========================================================
    // TENANT + EMAIL
    // =========================================================

    /**
     * Find a non-deleted user by tenant and email.
     *
     * <p>
     * This method is used when authentication or tenant-scoped
     * user lookup requires both the tenant and email.
     * </p>
     *
     * @param tenantId tenant ID
     * @param email email
     * @param isDeleted deleted flag
     * @return user if found
     */
    @EntityGraph(attributePaths = {
            "role"
    })
    Optional<User> findByTenant_IdAndEmailAndIsDeleted(
            Long tenantId,
            String email,
            Integer isDeleted
    );


    // =========================================================
    // TENANT + PUBLIC ID
    // =========================================================

    /**
     * Find a non-deleted user by tenant and public ID.
     *
     * <p>
     * This is important for tenant isolation. An ADMIN must not
     * be able to retrieve a user belonging to another tenant
     * simply by supplying that user's public ID.
     * </p>
     *
     * @param tenantId tenant ID
     * @param publicId user public ID
     * @param isDeleted deleted flag
     * @return user if found
     */
    @EntityGraph(attributePaths = {
            "role"
    })
    Optional<User> findByTenant_IdAndPublicIdAndIsDeleted(
            Long tenantId,
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // TENANT + USERNAME
    // =========================================================

    /**
     * Find a non-deleted user by tenant and username.
     *
     * @param tenantId tenant ID
     * @param username username
     * @param isDeleted deleted flag
     * @return user if found
     */
    Optional<User> findByTenant_IdAndUsernameAndIsDeleted(
            Long tenantId,
            String username,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - TENANT + USERNAME
    // =========================================================

    /**
     * Check whether a non-deleted user exists with the supplied
     * username inside the specified tenant.
     *
     * <p>
     * This prevents tenant A from being incorrectly considered
     * to have a duplicate username because the same username
     * exists in tenant B.
     * </p>
     *
     * @param tenantId tenant ID
     * @param username username
     * @param isDeleted deleted flag
     * @return true if username exists in tenant
     */
    boolean existsByTenant_IdAndUsernameAndIsDeleted(
            Long tenantId,
            String username,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - TENANT + EMAIL
    // =========================================================

    /**
     * Check whether a non-deleted user exists with the supplied
     * email inside the specified tenant.
     *
     * @param tenantId tenant ID
     * @param email email
     * @param isDeleted deleted flag
     * @return true if email exists in tenant
     */
    boolean existsByTenant_IdAndEmailAndIsDeleted(
            Long tenantId,
            String email,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - TENANT + USERNAME EXCLUDING CURRENT USER
    // =========================================================

    /**
     * Check whether another non-deleted user exists with the
     * supplied username inside the specified tenant.
     *
     * @param tenantId tenant ID
     * @param username username
     * @param isDeleted deleted flag
     * @param publicId current user's public ID
     * @return true if another user exists
     */
    boolean existsByTenant_IdAndUsernameAndIsDeletedAndPublicIdNot(
            Long tenantId,
            String username,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // EXISTS - TENANT + EMAIL EXCLUDING CURRENT USER
    // =========================================================

    /**
     * Check whether another non-deleted user exists with the
     * supplied email inside the specified tenant.
     *
     * @param tenantId tenant ID
     * @param email email
     * @param isDeleted deleted flag
     * @param publicId current user's public ID
     * @return true if another user exists
     */
    boolean existsByTenant_IdAndEmailAndIsDeletedAndPublicIdNot(
            Long tenantId,
            String email,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // TENANT USERS
    // =========================================================

    /**
     * Find all non-deleted users belonging to a tenant.
     *
     * @param tenantId tenant ID
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated tenant users
     */
    Page<User> findByTenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Finds a non-deleted user by public ID within a specific tenant.
     *
     * <p>
     * The tenant filter is applied in the database query itself,
     * so an administrator cannot load another tenant's user by
     * supplying that user's public ID.
     * </p>
     *
     * @param tenantId tenant database ID
     * @param publicId public ID of the target user
     * @param isDeleted deletion status
     * @return the user when found inside the specified tenant
     */
}