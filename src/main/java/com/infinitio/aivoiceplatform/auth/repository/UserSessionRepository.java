package com.infinitio.aivoiceplatform.auth.repository;

import com.infinitio.aivoiceplatform.auth.entity.UserSession;
import com.infinitio.aivoiceplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User Session entity.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface UserSessionRepository
        extends JpaRepository<UserSession, Long> {

    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    Optional<UserSession> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND ALL USER SESSIONS
    // =========================================================

    List<UserSession> findByUserAndIsDeleted(
            User user,
            Integer isDeleted
    );


    // =========================================================
    // FIND ACTIVE USER SESSIONS
    // =========================================================

    List<UserSession> findByUserAndActiveAndIsDeleted(
            User user,
            Boolean active,
            Integer isDeleted
    );


    // =========================================================
    // FIND ONE ACTIVE USER SESSION
    // =========================================================

    Optional<UserSession> findFirstByUserAndActiveAndIsDeleted(
            User user,
            Boolean active,
            Integer isDeleted
    );
}