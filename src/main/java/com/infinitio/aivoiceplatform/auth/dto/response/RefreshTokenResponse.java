package com.infinitio.aivoiceplatform.auth.dto.response;

import lombok.*;

/**
 * Refresh Token Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {

    /**
     * New JWT Access Token
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
     * Expiration Time
     */
    private Long expiresIn;

}