package com.infinitio.aivoiceplatform.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT Service Implementation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;


    // =========================================================
    // SIGNING KEY
    // =========================================================

    private SecretKey getSigningKey() {

        if (jwtSecret == null
                || jwtSecret.isBlank()) {

            throw new IllegalStateException(
                    "JWT secret is not configured."
            );
        }

        byte[] keyBytes =
                jwtSecret.getBytes(
                        StandardCharsets.UTF_8
                );

        if (keyBytes.length < 32) {

            throw new IllegalStateException(
                    "JWT secret must contain at least 32 bytes."
            );
        }

        return Keys.hmacShaKeyFor(
                keyBytes
        );
    }


    // =========================================================
    // ACCESS TOKEN
    // =========================================================

    @Override
    public String generateAccessToken(
            String username) {

        validateUsername(username);

        return generateToken(
                Map.of(),
                username,
                getAccessTokenExpiration()
        );
    }


    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    public String generateRefreshToken(
            String username) {

        validateUsername(username);

        return generateToken(
                Map.of(),
                username,
                getRefreshTokenExpiration()
        );
    }


    // =========================================================
    // GENERATE TOKEN
    // =========================================================

    @Override
    public String generateToken(
            Map<String, Object> claims,
            String username,
            long expiration) {

        validateUsername(username);

        if (expiration <= 0) {

            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero."
            );
        }

        Date issuedAt =
                new Date();

        Date expirationDate =
                new Date(
                        issuedAt.getTime()
                                + expiration
                );

        return Jwts.builder()
                .setClaims(
                        claims == null
                                ? Map.of()
                                : claims
                )
                .setSubject(username)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }


    // =========================================================
    // EXTRACT USERNAME
    // =========================================================

    @Override
    public String extractUsername(
            String token) {

        return extractAllClaims(
                token
        ).getSubject();
    }


    // =========================================================
    // EXTRACT CLAIMS
    // =========================================================

    @Override
    public Claims extractAllClaims(
            String token) {

        if (token == null
                || token.isBlank()) {

            throw new IllegalArgumentException(
                    "JWT token is required."
            );
        }

        return Jwts.parserBuilder()
                .setSigningKey(
                        getSigningKey()
                )
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    @Override
    public boolean isTokenValid(
            String token,
            String username) {

        if (token == null
                || token.isBlank()
                || username == null
                || username.isBlank()) {

            return false;
        }

        try {

            Claims claims =
                    extractAllClaims(
                            token
                    );

            String tokenUsername =
                    claims.getSubject();

            Date expiration =
                    claims.getExpiration();

            return username.equals(
                    tokenUsername
            )
                    && expiration != null
                    && expiration.after(
                    new Date()
            );

        } catch (Exception exception) {

            log.warn(
                    "JWT validation failed. type={}",
                    exception.getClass()
                            .getSimpleName()
            );

            return false;
        }
    }


    // =========================================================
    // EXPIRATION
    // =========================================================

    @Override
    public boolean isTokenExpired(
            String token) {

        try {

            Date expiration =
                    extractAllClaims(token)
                            .getExpiration();

            return expiration == null
                    || !expiration.after(
                    new Date()
            );

        } catch (Exception exception) {

            return true;
        }
    }


    // =========================================================
    // EXPIRATION VALUES
    // =========================================================

    @Override
    public long getAccessTokenExpiration() {

        if (accessTokenExpiration == null
                || accessTokenExpiration <= 0) {

            throw new IllegalStateException(
                    "JWT access-token expiration is not configured."
            );
        }

        return accessTokenExpiration;
    }


    @Override
    public long getRefreshTokenExpiration() {

        if (refreshTokenExpiration == null
                || refreshTokenExpiration <= 0) {

            throw new IllegalStateException(
                    "JWT refresh-token expiration is not configured."
            );
        }

        return refreshTokenExpiration;
    }


    // =========================================================
    // USERNAME VALIDATION
    // =========================================================

    private void validateUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "JWT username is required."
            );
        }
    }
}