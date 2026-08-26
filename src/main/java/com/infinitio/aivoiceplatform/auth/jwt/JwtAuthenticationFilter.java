package com.infinitio.aivoiceplatform.auth.jwt;

import com.infinitio.aivoiceplatform.auth.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * Extracts the JWT access token from the Authorization header,
 * validates the token and establishes the authenticated user
 * inside Spring Security SecurityContext.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER =
            "Authorization";

    private static final String BEARER_PREFIX =
            "Bearer ";


    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;


    // =========================================================
    // FILTER
    // =========================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        /*
         * Do not replace an authentication that has already
         * been established by another authentication mechanism.
         */
        if (SecurityContextHolder
                .getContext()
                .getAuthentication() != null) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        /*
         * Get Authorization header.
         */
        String authorizationHeader =
                request.getHeader(
                        AUTHORIZATION_HEADER
                );


        /*
         * No Authorization header.
         *
         * Public endpoints can continue normally.
         * Protected endpoints will be rejected by Spring Security.
         */
        if (!StringUtils.hasText(
                authorizationHeader)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        /*
         * Authorization header must contain Bearer token.
         */
        if (!authorizationHeader.startsWith(
                BEARER_PREFIX)) {

            log.warn(
                    "Invalid Authorization header format. URI={}",
                    request.getRequestURI()
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        /*
         * Extract JWT.
         */
        String jwt =
                authorizationHeader
                        .substring(
                                BEARER_PREFIX.length()
                        )
                        .trim();


        /*
         * Empty token.
         */
        if (!StringUtils.hasText(jwt)) {

            log.warn(
                    "Bearer token is empty. URI={}",
                    request.getRequestURI()
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        try {

            // =================================================
            // 1. EXTRACT USERNAME / EMAIL
            // =================================================

            String email =
                    jwtService.extractUsername(
                            jwt
                    );

            log.debug(
                    "JWT subject extracted. username={}",
                    email
            );


            /*
             * Subject is mandatory.
             */
            if (!StringUtils.hasText(email)) {

                log.warn(
                        "JWT subject is missing. URI={}",
                        request.getRequestURI()
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 2. LOAD CURRENT USER
            // =================================================

            UserDetails userDetails =
                    customUserDetailsService
                            .loadUserByUsername(
                                    email
                            );

            log.debug(
                    "User loaded successfully from database. username={}",
                    userDetails.getUsername()
            );


            // =================================================
            // 3. VALIDATE USER IDENTITY
            // =================================================

            if (!email.equalsIgnoreCase(
                    userDetails.getUsername())) {

                log.warn(
                        "JWT subject and database username do not match. jwtUser={}, databaseUser={}",
                        email,
                        userDetails.getUsername()
                );

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 4. CHECK USER ACCOUNT
            // =================================================

            if (!userDetails.isEnabled()) {

                log.warn(
                        "User account is disabled. username={}",
                        email
                );

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            if (!userDetails.isAccountNonLocked()) {

                log.warn(
                        "User account is locked. username={}",
                        email
                );

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            if (!userDetails.isAccountNonExpired()) {

                log.warn(
                        "User account is expired. username={}",
                        email
                );

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            if (!userDetails.isCredentialsNonExpired()) {

                log.warn(
                        "User credentials are expired. username={}",
                        email
                );

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 5. VALIDATE JWT
            // =================================================

            boolean valid =
                    jwtService.isTokenValid(
                            jwt,
                            userDetails.getUsername()
                    );

            log.debug(
                    "JWT validation result. username={}, valid={}",
                    email,
                    valid
            );


            if (!valid) {

                log.warn(
                        "JWT validation failed. username={}, URI={}",
                        email,
                        request.getRequestURI()
                );

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 6. CREATE AUTHENTICATION
            // =================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );


            // =================================================
            // 7. ATTACH REQUEST DETAILS
            // =================================================

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );


            // =================================================
            // 8. SET SECURITY CONTEXT
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


            log.info(
                    "JWT authentication established successfully. username={}, URI={}",
                    email,
                    request.getRequestURI()
            );


        } catch (Exception exception) {

            /*
             * Never expose JWT or sensitive authentication
             * information to the client.
             */
            SecurityContextHolder
                    .clearContext();


            /*
             * Keep full stack trace while debugging.
             *
             * IMPORTANT:
             * The JWT itself is never logged.
             */
            log.error(
                    "JWT authentication failed. URI={}, exceptionType={}, message={}",
                    request.getRequestURI(),
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception
            );
        }


        // =====================================================
        // CONTINUE SECURITY FILTER CHAIN
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}