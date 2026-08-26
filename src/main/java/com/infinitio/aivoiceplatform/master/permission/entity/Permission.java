package com.infinitio.aivoiceplatform.master.permission.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(
            name = "permission_code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String permissionCode;

    @Column(
            name = "permission_name",
            nullable = false,
            length = 150
    )
    private String permissionName;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;

    @Column(
            name = "is_system",
            nullable = false
    )
    private Integer isSystem;
}