package com.infinitio.aivoiceplatform.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Change Password Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required.")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    private String newPassword;

}