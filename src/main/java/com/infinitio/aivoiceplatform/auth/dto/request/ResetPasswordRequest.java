package com.infinitio.aivoiceplatform.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Reset Password Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required.")
    private String token;

    @NotBlank(message = "New password is required.")
    private String newPassword;

}