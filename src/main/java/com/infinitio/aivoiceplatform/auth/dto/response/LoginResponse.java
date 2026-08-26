package com.infinitio.aivoiceplatform.auth.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Login Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Access Token
     */
    private String accessToken;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Token Type
     */
    private String tokenType;

    /**
     * Access Token Expiration (Seconds)
     */
    private Long expiresIn;

    /**
     * User Details
     */
    private String userPublicId;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    /**
     * Organization Details
     */
    private String organizationPublicId;

    private String organizationName;

    /**
     * Role Details
     */
    private String rolePublicId;

    private String roleName;

    /**
     * Login Time
     */
    private LocalDateTime loginTime;

}