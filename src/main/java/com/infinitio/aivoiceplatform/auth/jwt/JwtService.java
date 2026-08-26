package com.infinitio.aivoiceplatform.auth.jwt;

import io.jsonwebtoken.Claims;

import java.util.Map;

/**
 * JWT Service.
 *
 * Provides JWT token generation and validation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface JwtService {

    /**
     * Generate Access Token.
     *
     * @param username User identifier
     * @return JWT Access Token
     */
    String generateAccessToken(
            String username
    );


    /**
     * Generate Refresh Token.
     *
     * @param username User identifier
     * @return JWT Refresh Token
     */
    String generateRefreshToken(
            String username
    );


    /**
     * Generate Token with Extra Claims.
     *
     * @param claims Claims
     * @param username Username
     * @param expiration Expiration duration in milliseconds
     * @return JWT
     */
    String generateToken(
            Map<String, Object> claims,
            String username,
            long expiration
    );


    /**
     * Extract Username.
     *
     * @param token JWT
     * @return Username
     */
    String extractUsername(
            String token
    );


    /**
     * Extract all claims.
     *
     * @param token JWT
     * @return Claims
     */
    Claims extractAllClaims(
            String token
    );


    /**
     * Validate Token.
     *
     * @param token JWT
     * @param username Username
     * @return true if valid
     */
    boolean isTokenValid(
            String token,
            String username
    );


    /**
     * Check Token Expiration.
     *
     * @param token JWT
     * @return true if expired
     */
    boolean isTokenExpired(
            String token
    );


    /**
     * Get configured access-token expiration.
     *
     * @return expiration in milliseconds
     */
    long getAccessTokenExpiration();


    /**
     * Get configured refresh-token expiration.
     *
     * @return expiration in milliseconds
     */
    long getRefreshTokenExpiration();
}