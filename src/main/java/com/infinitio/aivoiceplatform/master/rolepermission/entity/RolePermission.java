package com.infinitio.aivoiceplatform.master.rolepermission.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import com.infinitio.aivoiceplatform.master.role.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Role Permission Entity.
 *
 * Represents a Permission assigned to a Role.
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
        name = "role_permissions",

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_role_permission",
                        columnNames = {
                                "role_id",
                                "permission_id"
                        }
                )
        },

        indexes = {
                @Index(
                        name = "idx_role_permissions_role",
                        columnList = "role_id"
                ),
                @Index(
                        name = "idx_role_permissions_permission",
                        columnList = "permission_id"
                ),
                @Index(
                        name = "idx_role_permissions_active",
                        columnList = "is_active"
                ),
                @Index(
                        name = "idx_role_permissions_deleted",
                        columnList = "is_deleted"
                )
        }
)
public class RolePermission extends BaseEntity {

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "permission_id",
            nullable = false
    )
    private Permission permission;
}