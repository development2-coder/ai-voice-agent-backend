package com.infinitio.aivoiceplatform.auth.util;

import com.infinitio.aivoiceplatform.auth.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security Utility.
 *
 * Provides helper methods for accessing the
 * authenticated user from Spring Security.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * Get Current Authentication.
     *
     * @return Authentication
     */
    public static Authentication getAuthentication() {

        return SecurityContextHolder.getContext()
                .getAuthentication();

    }

    /**
     * Get Current User Principal.
     *
     * @return UserPrincipal
     */
    public static UserPrincipal getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof UserPrincipal)) {

            throw new RuntimeException(
                    "No authenticated user found.");

        }

        return (UserPrincipal)
                authentication.getPrincipal();

    }

}