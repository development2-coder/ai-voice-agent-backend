package com.infinitio.aivoiceplatform.master.role.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Role Entity.
 *
 * Stores application roles.
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
@Table(name = "roles")
public class Role extends BaseEntity {

    /**
     * Role Code.
     */
    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    /**
     * Role Name.
     */
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /**
     * Description.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Display Order.
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * System Role.
     */
    @Column(name = "is_system", nullable = false)
    private Integer isSystem;

    /**
     * Default Role.
     */
    @Column(name = "is_default", nullable = false)
    private Integer isDefault;

}