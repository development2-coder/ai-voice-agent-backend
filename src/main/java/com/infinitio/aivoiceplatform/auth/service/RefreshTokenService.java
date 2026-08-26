package com.infinitio.aivoiceplatform.auth.service;

import com.infinitio.aivoiceplatform.auth.entity.RefreshToken;
import com.infinitio.aivoiceplatform.user.entity.User;

/**
 * Refresh Token Service.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RefreshTokenService {

    RefreshToken createRefreshToken(
            User user
    );

    RefreshToken validateRefreshToken(
            String refreshToken
    );

    void revokeRefreshToken(
            String refreshToken
    );

    void revokeAllTokens(
            User user
    );

    RefreshToken rotateRefreshToken(
            String refreshToken
    );
}