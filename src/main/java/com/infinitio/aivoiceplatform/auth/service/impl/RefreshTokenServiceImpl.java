package com.infinitio.aivoiceplatform.auth.service.impl;

import com.infinitio.aivoiceplatform.auth.constant.AuthMessages;
import com.infinitio.aivoiceplatform.auth.entity.RefreshToken;
import com.infinitio.aivoiceplatform.auth.jwt.JwtService;
import com.infinitio.aivoiceplatform.auth.repository.RefreshTokenRepository;
import com.infinitio.aivoiceplatform.auth.service.RefreshTokenService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Refresh Token Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private static final Integer NOT_DELETED = 0;
    private static final Integer ACTIVE = 1;
    private static final Boolean NOT_REVOKED = false;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtService jwtService;


    @Override
    public RefreshToken createRefreshToken(
            User user) {

        if (user == null) {

            throw new BadRequestException(
                    "User is required to create refresh token."
            );
        }

        if (!NOT_DELETED.equals(
                user.getIsDeleted())
                || !ACTIVE.equals(
                user.getIsActive())) {

            throw new BadRequestException(
                    "User is not active."
            );
        }

        String token =
                jwtService.generateRefreshToken(
                        user.getEmail()
                );

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plus(
                                Duration.ofMillis(
                                        jwtService
                                                .getRefreshTokenExpiration()
                                )
                        );

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .user(user)
                        .refreshToken(token)
                        .expiresAt(expiresAt)
                        .revoked(false)
                        .build();

        /*
         * Login happens before SecurityContext exists.
         */
        refreshToken.setCreatedBy(
                user.getId()
        );

        RefreshToken savedToken =
                refreshTokenRepository.save(
                        refreshToken
                );

        log.info(
                "Refresh token created. tokenPublicId={}",
                savedToken.getPublicId()
        );

        return savedToken;
    }


    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(
            String token) {

        if (token == null
                || token.isBlank()) {

            throw new BadRequestException(
                    AuthMessages.INVALID_REFRESH_TOKEN
            );
        }

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByRefreshTokenAndRevokedAndIsDeleted(
                                token,
                                NOT_REVOKED,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        AuthMessages.INVALID_REFRESH_TOKEN
                                )
                        );

        if (refreshToken.getExpiresAt() == null
                || !refreshToken
                .getExpiresAt()
                .isAfter(
                        LocalDateTime.now()
                )) {

            throw new BadRequestException(
                    AuthMessages.REFRESH_TOKEN_EXPIRED
            );
        }

        User user =
                refreshToken.getUser();

        if (user == null
                || !NOT_DELETED.equals(
                user.getIsDeleted())
                || !ACTIVE.equals(
                user.getIsActive())) {

            throw new BadRequestException(
                    "User is not active."
            );
        }

        if (Boolean.TRUE.equals(
                user.getAccountLocked())) {

            throw new BadRequestException(
                    AuthMessages.ACCOUNT_LOCKED
            );
        }

        return refreshToken;
    }


    @Override
    public void revokeRefreshToken(
            String token) {

        RefreshToken refreshToken =
                validateRefreshToken(token);

        refreshToken.setRevoked(true);
        refreshToken.setUpdatedBy(
                refreshToken.getUser().getId()
        );

        refreshTokenRepository.save(
                refreshToken
        );
    }


    @Override
    public void revokeAllTokens(
            User user) {

        if (user == null) {
            return;
        }

        List<RefreshToken> tokens =
                refreshTokenRepository
                        .findByUserAndRevokedAndIsDeleted(
                                user,
                                NOT_REVOKED,
                                NOT_DELETED
                        );

        if (tokens.isEmpty()) {
            return;
        }

        Long updatedBy =
                user.getId();

        tokens.forEach(token -> {

            token.setRevoked(true);
            token.setUpdatedBy(updatedBy);

        });

        refreshTokenRepository.saveAll(tokens);

        log.info(
                "All active refresh tokens revoked. userPublicId={}",
                user.getPublicId()
        );
    }


    @Override
    public RefreshToken rotateRefreshToken(
            String token) {

        RefreshToken currentToken =
                validateRefreshToken(token);

        User user =
                currentToken.getUser();

        currentToken.setRevoked(true);
        currentToken.setUpdatedBy(
                user.getId()
        );

        refreshTokenRepository.save(
                currentToken
        );

        return createRefreshToken(user);
    }
}