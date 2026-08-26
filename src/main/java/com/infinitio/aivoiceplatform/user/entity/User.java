package com.infinitio.aivoiceplatform.user.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * User Entity.
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
@Table(name = "users")
public class User extends BaseEntity {

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tenant_id",
            nullable = false
    )
    private Tenant tenant;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private Organization organization;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @Column(
            name = "password",
            nullable = false,
            length = 500
    )
    private String password;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "middle_name",
            length = 100
    )
    private String middleName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;

    @Column(
            name = "full_name",
            nullable = false,
            length = 300
    )
    private String fullName;

    @Column(
            name = "mobile_number",
            length = 20
    )
    private String mobileNumber;

    @Column(
            name = "designation",
            length = 150
    )
    private String designation;

    @Column(
            name = "department",
            length = 150
    )
    private String department;

    @Column(
            name = "profile_image",
            length = 500
    )
    private String profileImage;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Builder.Default
    @Column(
            name = "failed_login_attempts",
            nullable = false
    )
    private Integer failedLoginAttempts = 0;

    @Builder.Default
    @Column(
            name = "account_locked",
            nullable = false
    )
    private Boolean accountLocked = false;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Builder.Default
    @Column(
            name = "email_verified",
            nullable = false
    )
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(
            name = "mobile_verified",
            nullable = false
    )
    private Boolean mobileVerified = false;


    // =========================================================
    // FULL NAME
    // =========================================================

    public void updateFullName() {

        StringBuilder name =
                new StringBuilder();

        appendName(
                name,
                firstName
        );

        appendName(
                name,
                middleName
        );

        appendName(
                name,
                lastName
        );

        this.fullName =
                name.toString().trim();
    }


    private void appendName(
            StringBuilder name,
            String value) {

        if (value == null
                || value.isBlank()) {

            return;
        }

        if (!name.isEmpty()) {
            name.append(" ");
        }

        name.append(
                value.trim()
        );
    }
}