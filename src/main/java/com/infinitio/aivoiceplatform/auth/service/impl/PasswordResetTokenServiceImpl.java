package com.infinitio.aivoiceplatform.auth.service.impl;

import com.infinitio.aivoiceplatform.auth.dto.response.PasswordResetTokenResult;
import com.infinitio.aivoiceplatform.auth.entity.PasswordResetToken;
import com.infinitio.aivoiceplatform.auth.repository.PasswordResetTokenRepository;
import com.infinitio.aivoiceplatform.auth.service.PasswordResetTokenService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * Password Reset Token Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetTokenServiceImpl
        implements PasswordResetTokenService {

    private static final Integer NOT_DELETED = 0;

    private static final Integer ACTIVE = 1;

    private static final Boolean NOT_USED = false;

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${auth.password-reset.token-expiration}")
    private Long tokenExpiration;


    // =========================================================
    // CREATE TOKEN
    // =========================================================

    @Override
    public PasswordResetTokenResult createToken(
            User user) {

        if (user == null) {

            throw new BadRequestException(
                    "User is required."
            );
        }

        if (!Integer.valueOf(NOT_DELETED)
                .equals(user.getIsDeleted())
                || !Integer.valueOf(ACTIVE)
                .equals(user.getIsActive())) {

            throw new BadRequestException(
                    "User is not active."
            );
        }

        if (tokenExpiration == null
                || tokenExpiration <= 0) {

            throw new IllegalStateException(
                    "Password reset token expiration is not configured."
            );
        }

        /*
         * Invalidate any previous active reset tokens.
         */
        invalidateUserTokens(user);

        /*
         * Generate a cryptographically secure random token.
         */
        byte[] tokenBytes =
                new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(tokenBytes);

        /*
         * Raw token is only returned to the caller.
         * It is NEVER persisted.
         */
        String rawToken =
                HexFormat.of().formatHex(tokenBytes);

        /*
         * Only the SHA-256 hash is stored.
         */
        String tokenHash =
                hashToken(rawToken);

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plus(
                                java.time.Duration.ofMillis(
                                        tokenExpiration
                                )
                        );

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .user(user)
                        .tokenHash(tokenHash)
                        .expiresAt(expiresAt)
                        .used(false)
                        .build();

        resetToken.setCreatedBy(
                user.getId()
        );

        PasswordResetToken savedToken =
                passwordResetTokenRepository.save(
                        resetToken
                );

        log.info(
                "Password reset token created. userPublicId={}",
                user.getPublicId()
        );

        return new PasswordResetTokenResult(
                savedToken,
                rawToken
        );
    }


    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(
            String rawToken) {

        if (rawToken == null
                || rawToken.isBlank()) {

            throw new BadRequestException(
                    "Password reset token is required."
            );
        }

        String tokenHash =
                hashToken(rawToken);

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHashAndUsedAndIsDeleted(
                                tokenHash,
                                NOT_USED,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Invalid password reset token."
                                )
                        );

        /*
         * Token must not be expired.
         */
        if (resetToken.getExpiresAt() == null
                || !resetToken
                .getExpiresAt()
                .isAfter(
                        LocalDateTime.now()
                )) {

            throw new BadRequestException(
                    "Password reset token has expired."
            );
        }

        User user =
                resetToken.getUser();

        /*
         * User must still be active.
         */
        if (user == null
                || !Integer.valueOf(NOT_DELETED)
                .equals(user.getIsDeleted())
                || !Integer.valueOf(ACTIVE)
                .equals(user.getIsActive())) {

            throw new ResourceNotFoundException(
                    "User is not available."
            );
        }

        return resetToken;
    }


    // =========================================================
    // INVALIDATE TOKEN
    // =========================================================

    @Override
    public void invalidateToken(
            PasswordResetToken token) {

        if (token == null) {
            return;
        }

        token.setUsed(true);
        token.setIsActive(0);

        passwordResetTokenRepository.save(
                token
        );

        log.info(
                "Password reset token invalidated. tokenPublicId={}",
                token.getPublicId()
        );
    }


    // =========================================================
    // INVALIDATE USER TOKENS
    // =========================================================

    @Override
    public void invalidateUserTokens(
            User user) {

        if (user == null) {
            return;
        }

        List<PasswordResetToken> tokens =
                passwordResetTokenRepository
                        .findByUserAndUsedAndIsDeleted(
                                user,
                                NOT_USED,
                                NOT_DELETED
                        );

        if (tokens.isEmpty()) {
            return;
        }

        tokens.forEach(token -> {

            token.setUsed(true);
            token.setIsActive(0);
            token.setUpdatedBy(
                    user.getId()
            );

        });

        passwordResetTokenRepository.saveAll(
                tokens
        );

        log.info(
                "Existing password reset tokens invalidated. userPublicId={}",
                user.getPublicId()
        );
    }


    // =========================================================
    // HASH TOKEN
    // =========================================================

    private String hashToken(
            String rawToken) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    exception
            );
        }
    }
}