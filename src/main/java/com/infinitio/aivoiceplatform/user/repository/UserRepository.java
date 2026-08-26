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
 * Handles database operations for User.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {


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
     * Role is fetched together with User because
     * Spring Security requires role authorities
     * during JWT authentication.
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
     * Used for first-user creation logic.
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
     * Example:
     * SUPER_ADMIN
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
     * Used during user update validation.
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
     * Used during user update validation.
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
}