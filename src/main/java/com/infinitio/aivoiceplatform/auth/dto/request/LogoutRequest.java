package com.infinitio.aivoiceplatform.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Logout Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required.")
    private String refreshToken;

}