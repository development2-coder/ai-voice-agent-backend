package com.infinitio.aivoiceplatform.master.userpermission.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import com.infinitio.aivoiceplatform.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Stores permissions assigned directly to an individual user.
 *
 * A user's permissions must also be allowed by the shared ADMIN role.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_permission",
                        columnNames = {"user_id", "permission_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_permissions_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_permissions_permission",
                        columnList = "permission_id"
                )
        }
)
public class UserPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}