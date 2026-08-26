package com.infinitio.aivoiceplatform.auth.repository;

import com.infinitio.aivoiceplatform.auth.entity.PasswordResetToken;
import com.infinitio.aivoiceplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Password Reset Token entity.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken>
    findByTokenHashAndUsedAndIsDeleted(
            String tokenHash,
            Boolean used,
            Integer isDeleted
    );

    List<PasswordResetToken>
    findByUserAndUsedAndIsDeleted(
            User user,
            Boolean used,
            Integer isDeleted
    );
}