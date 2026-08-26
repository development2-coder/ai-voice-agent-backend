package com.infinitio.aivoiceplatform.auth.controller;

import com.infinitio.aivoiceplatform.auth.constant.AuthMessages;
import com.infinitio.aivoiceplatform.auth.dto.request.ChangePasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.ForgotPasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.LoginRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.LogoutRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.RefreshTokenRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.ResetPasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.response.LoginResponse;
import com.infinitio.aivoiceplatform.auth.dto.response.RefreshTokenResponse;
import com.infinitio.aivoiceplatform.auth.service.AuthService;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller.
 *
 * Exposes authentication APIs.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("REST request received: login.");

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMessages.LOGIN_SUCCESS,
                        response
                )
        );
    }


    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info(
                "REST request received: refresh token."
        );

        RefreshTokenResponse response =
                authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMessages.TOKEN_REFRESH_SUCCESS,
                        response
                )
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {

        log.info("REST request received: logout.");

        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMessages.LOGOUT_SUCCESS,
                        null
                )
        );
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info(
                "REST request received: change password."
        );

        authService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMessages.PASSWORD_CHANGED,
                        null
                )
        );
    }


    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info(
                "REST request received: forgot password."
        );

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMessages.PASSWORD_RESET_LINK_SENT,
                        null
                )
        );
    }


    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info(
                "REST request received: reset password."
        );

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        AuthMessages.PASSWORD_RESET_SUCCESS,
                        null
                )
        );
    }
}