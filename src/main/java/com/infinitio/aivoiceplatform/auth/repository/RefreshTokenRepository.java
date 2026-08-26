package com.infinitio.aivoiceplatform.auth.repository;

import com.infinitio.aivoiceplatform.auth.entity.RefreshToken;
import com.infinitio.aivoiceplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Refresh Token entity.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    // =========================================================
    // FIND TOKEN
    // =========================================================

    Optional<RefreshToken> findByRefreshTokenAndIsDeleted(
            String refreshToken,
            Integer isDeleted
    );


    // =========================================================
    // FIND USER TOKENS
    // =========================================================

    List<RefreshToken> findByUserAndIsDeleted(
            User user,
            Integer isDeleted
    );


    // =========================================================
    // FIND NON-REVOKED TOKEN
    // =========================================================

    Optional<RefreshToken>
    findByRefreshTokenAndRevokedAndIsDeleted(
            String refreshToken,
            Boolean revoked,
            Integer isDeleted
    );


    // =========================================================
    // FIND ALL NON-REVOKED USER TOKENS
    // =========================================================

    List<RefreshToken>
    findByUserAndRevokedAndIsDeleted(
            User user,
            Boolean revoked,
            Integer isDeleted
    );
}