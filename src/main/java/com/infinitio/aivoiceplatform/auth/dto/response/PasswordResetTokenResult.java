package com.infinitio.aivoiceplatform.auth.dto.response;

import com.infinitio.aivoiceplatform.auth.entity.PasswordResetToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Password Reset Token Result.
 *
 * Contains the persisted reset-token entity and the raw token.
 *
 * The raw token must only be used for the reset link/email
 * and must never be persisted or logged.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public class PasswordResetTokenResult {

    private final PasswordResetToken passwordResetToken;

    private final String rawToken;
}