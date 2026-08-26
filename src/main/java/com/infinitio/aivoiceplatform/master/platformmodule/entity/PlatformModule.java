package com.infinitio.aivoiceplatform.master.platformmodule.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a functional module in the AI Voice Platform.
 *
 * Example:
 * Dashboard
 * User Management
 * Campaign
 * AI Agent
 * AI Dialer
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "modules",
        indexes = {
                @Index(name = "ix_modules_module_code", columnList = "module_code"),
                @Index(name = "ix_modules_module_name", columnList = "module_name"),
                @Index(name = "ix_modules_display_order", columnList = "display_order"),
                @Index(name = "ix_modules_is_active", columnList = "is_active"),
                @Index(name = "ix_modules_is_deleted", columnList = "is_deleted")
        }
)
public class PlatformModule extends BaseEntity {

    @Column(name = "module_code", nullable = false, unique = true, length = 50)
    private String moduleCode;

    @Column(name = "module_name", nullable = false, unique = true, length = 150)
    private String moduleName;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "route", length = 255)
    private String route;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_system", nullable = false)
    private Integer isSystem;

    @Column(name = "is_visible", nullable = false)
    private Integer isVisible;

}