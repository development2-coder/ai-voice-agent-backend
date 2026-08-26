package com.infinitio.aivoiceplatform.auth.validator;

import com.infinitio.aivoiceplatform.auth.constant.AuthMessages;
import com.infinitio.aivoiceplatform.auth.dto.request.ChangePasswordRequest;
import com.infinitio.aivoiceplatform.auth.dto.request.LoginRequest;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Authentication Validator.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AuthValidator {

    private static final Integer NOT_DELETED = 0;
    private static final Integer ACTIVE = 1;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // LOGIN
    // =========================================================

    public User validateLogin(
            LoginRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Login request is required."
            );
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {

            throw new BadRequestException(
                    "Email is required."
            );
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new BadRequestException(
                    "Password is required."
            );
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
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        AuthMessages.INVALID_CREDENTIALS
                                )
                        );

        if (!Integer.valueOf(ACTIVE)
                .equals(user.getIsActive())) {

            throw new BadRequestException(
                    AuthMessages.ACCOUNT_DISABLED
            );
        }

        if (Boolean.TRUE.equals(
                user.getAccountLocked())) {

            LocalDateTime lockedUntil =
                    user.getAccountLockedUntil();

            if (lockedUntil == null
                    || lockedUntil.isAfter(
                    LocalDateTime.now()
            )) {

                throw new BadRequestException(
                        AuthMessages.ACCOUNT_LOCKED
                );
            }

            /*
             * Temporary lock has expired.
             */
            user.setAccountLocked(false);
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);

            userRepository.save(user);
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    AuthMessages.INVALID_CREDENTIALS
            );
        }

        return user;
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    public void validateChangePassword(
            User user,
            ChangePasswordRequest request) {

        if (user == null) {

            throw new BadRequestException(
                    "User is required."
            );
        }

        if (request == null) {

            throw new BadRequestException(
                    "Change password request is required."
            );
        }

        if (request.getCurrentPassword() == null
                || request.getCurrentPassword().isBlank()) {

            throw new BadRequestException(
                    "Current password is required."
            );
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {

            throw new BadRequestException(
                    "New password is required."
            );
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new BadRequestException(
                    AuthMessages.INVALID_CREDENTIALS
            );
        }

        if (request.getCurrentPassword()
                .equals(
                        request.getNewPassword()
                )) {

            throw new BadRequestException(
                    "New password must be different from current password."
            );
        }
    }
}