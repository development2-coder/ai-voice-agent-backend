package com.infinitio.aivoiceplatform.auth.service;

import com.infinitio.aivoiceplatform.auth.dto.request.ChangePasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.ForgotPasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.LoginRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.LogoutRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.RefreshTokenRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.ResetPasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.response.LoginResponse;
import com.infinitio.aivoiceplatform.auth.dto.response.RefreshTokenResponse;

/**
 * Authentication Service.
 *
 * Handles authentication related operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface AuthService {

    /**
     * Login User.
     *
     * @param request Login Request
     * @return Login Response
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refresh Access Token.
     *
     * @param request Refresh Token Request
     * @return Refresh Token Response
     */
    RefreshTokenResponse refreshToken(
            RefreshTokenRequest request);

    /**
     * Logout User.
     *
     * @param request Logout Request
     */
    void logout(LogoutRequest request);

    /**
     * Forgot Password.
     *
     * @param request Forgot Password Request
     */
    void forgotPassword(
            ForgotPasswordRequest request);

    /**
     * Reset Password.
     *
     * @param request Reset Password Request
     */
    void resetPassword(
            ResetPasswordRequest request);

    /**
     * Change Password.
     *
     * @param request Change Password Request
     */
    void changePassword(
            ChangePasswordRequest request);

}