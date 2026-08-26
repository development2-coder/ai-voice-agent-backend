package com.infinitio.aivoiceplatform.auth.service;

import com.infinitio.aivoiceplatform.auth.dto.response.PasswordResetTokenResult;
import com.infinitio.aivoiceplatform.auth.entity.PasswordResetToken;
import com.infinitio.aivoiceplatform.user.entity.User;

/**
 * Password Reset Token Service.
 *
 * Handles password reset token creation,
 * validation and invalidation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface PasswordResetTokenService {

    /**
     * Creates a new password reset token.
     *
     * @param user User requesting password reset
     * @return token result containing persisted entity
     *         and raw token
     */
    PasswordResetTokenResult createToken(
            User user
    );

    /**
     * Validates a raw password reset token.
     *
     * @param rawToken Raw token received from client
     * @return validated password reset token
     */
    PasswordResetToken validateToken(
            String rawToken
    );

    /**
     * Invalidates a password reset token.
     *
     * @param token Password reset token
     */
    void invalidateToken(
            PasswordResetToken token
    );

    /**
     * Invalidates all unused reset tokens
     * belonging to a user.
     *
     * @param user User
     */
    void invalidateUserTokens(
            User user
    );
}