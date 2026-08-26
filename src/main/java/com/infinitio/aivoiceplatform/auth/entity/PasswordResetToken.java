package com.infinitio.aivoiceplatform.auth.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Password Reset Token Entity.
 *
 * Stores password reset token information for users.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
                @Index(
                        name = "idx_password_reset_token_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_password_reset_token_expiry",
                        columnList = "expires_at"
                )
        }
)
public class PasswordResetToken extends BaseEntity {

    // =========================================================
    // USER
    // =========================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    // =========================================================
    // TOKEN HASH
    // =========================================================

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 255
    )
    private String tokenHash;


    // =========================================================
    // EXPIRATION
    // =========================================================

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;


    // =========================================================
    // USED
    // =========================================================

    @Column(
            name = "used",
            nullable = false
    )
    private Boolean used = false;
}