package com.infinitio.aivoiceplatform.auth.service.impl;

import com.infinitio.aivoiceplatform.auth.constant.AuthConstants;
import com.infinitio.aivoiceplatform.auth.dto.request.ChangePasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.ForgotPasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.LoginRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.LogoutRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.RefreshTokenRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.ResetPasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.response.LoginResponse;
import com.infinitio.aivoiceplatform.auth.dto.response.PasswordResetTokenResult;
import com.infinitio.aivoiceplatform.auth.dto.response.RefreshTokenResponse;
import com.infinitio.aivoiceplatform.auth.entity.PasswordResetToken;
import com.infinitio.aivoiceplatform.auth.entity.RefreshToken;
import com.infinitio.aivoiceplatform.auth.entity.UserSession;
import com.infinitio.aivoiceplatform.auth.enums.DeviceType;
import com.infinitio.aivoiceplatform.auth.enums.LoginType;
import com.infinitio.aivoiceplatform.auth.jwt.JwtService;
import com.infinitio.aivoiceplatform.auth.service.AuthService;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.auth.service.PasswordResetTokenService;
import com.infinitio.aivoiceplatform.auth.service.RefreshTokenService;
import com.infinitio.aivoiceplatform.auth.service.UserSessionService;
import com.infinitio.aivoiceplatform.auth.validator.AuthValidator;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Authentication Service Implementation.
 *
 * Handles authentication business logic.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Integer NOT_DELETED = 0;

    private final UserRepository userRepository;

    private final AuthValidator authValidator;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final UserSessionService userSessionService;

    private final CurrentUserService currentUserService;

    private final PasswordEncoder passwordEncoder;

    private final HttpServletRequest httpServletRequest;

    private final PasswordResetTokenService passwordResetTokenService;

    /*
     * Access-token expiration comes from application
     * configuration. No hardcoded 3600 value.
     */
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    /*
     * Frontend reset-password URL comes from configuration.
     */
    @Value("${auth.password-reset.frontend-url}")
    private String passwordResetFrontendUrl;


    // =========================================================
    // LOGIN
    // =========================================================

    @Override
    public LoginResponse login(
            LoginRequest request) {

        log.info("Login request received.");

        /*
         * AuthValidator handles:
         *
         * - user existence
         * - password validation
         * - active status
         * - deleted status
         */
        User user =
                authValidator.validateLogin(
                        request
                );

        /*
         * Generate access token.
         */
        String accessToken =
                jwtService.generateAccessToken(
                        user.getEmail()
                );

        /*
         * Create persisted refresh token.
         */
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        user
                );

        /*
         * Read actual client information.
         */
        String ipAddress =
                getClientIpAddress(
                        httpServletRequest
                );

        String userAgent =
                httpServletRequest.getHeader(
                        "User-Agent"
                );

        /*
         * Determine device type from User-Agent.
         */
        DeviceType deviceType =
                resolveDeviceType(
                        userAgent
                );

        /*
         * WEB is the login channel of this
         * HTTP authentication endpoint.
         *
         * Device type is dynamically detected.
         */
        UserSession session =
                userSessionService.createSession(
                        user,
                        LoginType.WEB,
                        deviceType,
                        ipAddress,
                        userAgent
                );

        log.info(
                "Login successful. userPublicId={}",
                user.getPublicId()
        );

        return LoginResponse.builder()
                .accessToken(
                        accessToken
                )
                .refreshToken(
                        refreshToken.getRefreshToken()
                )
                .tokenType(
                        AuthConstants.TOKEN_TYPE
                )
                .expiresIn(
                        accessTokenExpiration
                )
                .userPublicId(
                        user.getPublicId()
                )
                .username(
                        user.getUsername()
                )
                .email(
                        user.getEmail()
                )
                .firstName(
                        user.getFirstName()
                )
                .lastName(
                        user.getLastName()
                )
                .organizationPublicId(
                        user.getOrganization()
                                .getPublicId()
                )
                .organizationName(
                        user.getOrganization()
                                .getOrganizationName()
                )
                .rolePublicId(
                        user.getRole()
                                .getPublicId()
                )
                .roleName(
                        user.getRole()
                                .getRoleName()
                )
                .loginTime(
                        session.getLoginTime()
                )
                .build();
    }


    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    public RefreshTokenResponse refreshToken(
            RefreshTokenRequest request) {

        log.info(
                "Refresh token request received."
        );

        RefreshToken newRefreshToken =
                refreshTokenService.rotateRefreshToken(
                        request.getRefreshToken()
                );

        User user =
                newRefreshToken.getUser();

        String accessToken =
                jwtService.generateAccessToken(
                        user.getEmail()
                );

        log.info(
                "Refresh token rotated successfully. userPublicId={}",
                user.getPublicId()
        );

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(
                        newRefreshToken.getRefreshToken()
                )
                .tokenType(
                        AuthConstants.TOKEN_TYPE
                )
                .expiresIn(
                        jwtService.getAccessTokenExpiration()
                )
                .build();
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @Override
    public void logout(
            LogoutRequest request) {

        log.info(
                "Logout request received."
        );

        /*
         * Validate refresh token first.
         */
        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(
                        request.getRefreshToken()
                );

        User user =
                refreshToken.getUser();

        /*
         * Revoke the supplied refresh token.
         */
        refreshTokenService.revokeRefreshToken(
                request.getRefreshToken()
        );

        /*
         * Current logout behavior:
         *
         * Logout all sessions and revoke all refresh
         * tokens for the user.
         */
        refreshTokenService.revokeAllTokens(
                user
        );

        userSessionService.logoutAll(
                user
        );

        log.info(
                "Logout completed. userPublicId={}",
                user.getPublicId()
        );
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Override
    public void changePassword(
            ChangePasswordRequest request) {

        log.info(
                "Change password request received."
        );

        /*
         * Get authenticated user.
         */
        User user =
                currentUserService.getCurrentUser();

        /*
         * Validate current password and new password.
         */
        authValidator.validateChangePassword(
                user,
                request
        );

        /*
         * Encode new password.
         */
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        /*
         * Update audit information.
         */
        user.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        userRepository.save(
                user
        );

        /*
         * Password change invalidates existing
         * authentication sessions/tokens.
         */
        userSessionService.logoutAll(
                user
        );

        refreshTokenService.revokeAllTokens(
                user
        );

        log.info(
                "Password changed successfully. userPublicId={}",
                user.getPublicId()
        );
    }


    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @Override
    public void forgotPassword(
            ForgotPasswordRequest request) {

        log.info(
                "Forgot password request received."
        );

        if (request == null
                || request.getEmail() == null
                || request.getEmail().isBlank()) {

            return;
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        User user =
                userRepository
                        .findByEmailAndIsDeleted(
                                email,
                                NOT_DELETED
                        )
                        .orElse(null);

        /*
         * Always return the same response to the client.
         * This prevents user/email enumeration.
         */
        if (user == null) {

            log.info(
                    "Password reset requested for unknown email."
            );

            return;
        }

        PasswordResetTokenResult tokenResult =
                passwordResetTokenService.createToken(
                        user
                );

        String resetUrl =
                buildResetUrl(
                        tokenResult.getRawToken()
                );

        /*
         * Do not log the raw token or reset URL.
         *
         * Later connect:
         *
         * emailService.sendPasswordResetEmail(
         *      user.getEmail(),
         *      resetUrl
         * );
         */

        log.info(
                "Password reset token generated. userPublicId={}",
                user.getPublicId()
        );
    }


    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Override
    public void resetPassword(
            ResetPasswordRequest request) {

        log.info(
                "Reset password request received."
        );

        /*
         * Validate raw reset token.
         *
         * PasswordResetTokenService:
         *
         * - hashes the supplied token
         * - finds the stored hash
         * - checks used state
         * - checks deleted state
         * - checks expiry
         * - checks user state
         */
        PasswordResetToken resetToken =
                passwordResetTokenService.validateToken(
                        request.getToken()
                );

        User user =
                resetToken.getUser();

        /*
         * Encode new password.
         */
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        /*
         * Reset-password is an unauthenticated
         * operation, so we do not fabricate a
         * createdBy/updatedBy user.
         *
         * JPA auditing/BaseEntity handles timestamps
         * according to the existing project configuration.
         */
        userRepository.save(
                user
        );

        /*
         * Invalidate reset token immediately.
         *
         * Prevents token reuse.
         */
        passwordResetTokenService.invalidateToken(
                resetToken
        );

        /*
         * Security measure:
         *
         * Password reset invalidates all existing
         * refresh tokens and sessions.
         */
        refreshTokenService.revokeAllTokens(
                user
        );

        userSessionService.logoutAll(
                user
        );

        log.info(
                "Password reset completed. userPublicId={}",
                user.getPublicId()
        );
    }


    // =========================================================
    // BUILD RESET URL
    // =========================================================

    private String buildResetUrl(
            String rawToken) {

        if (passwordResetFrontendUrl == null
                || passwordResetFrontendUrl.isBlank()) {

            throw new IllegalStateException(
                    "Password reset frontend URL is not configured."
            );
        }

        if (rawToken == null
                || rawToken.isBlank()) {

            throw new IllegalStateException(
                    "Password reset token was not generated."
            );
        }

        return passwordResetFrontendUrl
                + "?token="
                + rawToken;
    }


    // =========================================================
    // CLIENT IP ADDRESS
    // =========================================================

    private String getClientIpAddress(
            HttpServletRequest request) {

        /*
         * Reverse proxy/load balancer support.
         */
        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        String realIp =
                request.getHeader(
                        "X-Real-IP"
                );

        if (realIp != null
                && !realIp.isBlank()) {

            return realIp;
        }

        return request.getRemoteAddr();
    }


    // =========================================================
    // DEVICE TYPE
    // =========================================================

    private DeviceType resolveDeviceType(
            String userAgent) {

        if (userAgent == null
                || userAgent.isBlank()) {

            return DeviceType.DESKTOP;
        }

        String agent =
                userAgent.toLowerCase(
                        Locale.ROOT
                );

        if (agent.contains("mobile")
                || agent.contains("android")
                || agent.contains("iphone")
                || agent.contains("ipad")) {

            return DeviceType.MOBILE;
        }

        return DeviceType.DESKTOP;
    }
}