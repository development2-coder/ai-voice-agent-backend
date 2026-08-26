package com.infinitio.aivoiceplatform.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Refresh Token Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required.")
    private String refreshToken;

}