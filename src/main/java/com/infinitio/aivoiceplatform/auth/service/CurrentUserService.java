package com.infinitio.aivoiceplatform.auth.service;

import com.infinitio.aivoiceplatform.exception.UnauthorizedException;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Provides information about the currently authenticated user.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private static final Integer NOT_DELETED = 0;
    private static final Integer ACTIVE = 1;

    private final UserRepository userRepository;


    // =========================================================
    // AUTHENTICATION CHECK
    // =========================================================

    public boolean isAuthenticated() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        if (!authentication.isAuthenticated()) {
            return false;
        }

        if ("anonymousUser".equals(
                authentication.getPrincipal())) {

            return false;
        }

        return authentication.getPrincipal() != null;
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(
                authentication.getPrincipal())) {

            throw new UnauthorizedException(
                    "User is not authenticated."
            );
        }

        String email =
                authentication.getName();

        if (email == null
                || email.isBlank()) {

            throw new UnauthorizedException(
                    "Authenticated user information is unavailable."
            );
        }

        return userRepository
                .findByEmailAndIsDeleted(
                        email,
                        NOT_DELETED
                )
                .filter(user ->
                        ACTIVE.equals(
                                user.getIsActive()
                        )
                )
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Authenticated user is not available."
                        )
                );
    }


    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }


    public String getCurrentUserPublicId() {
        return getCurrentUser().getPublicId();
    }


    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }
}