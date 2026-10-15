package com.infinitio.aivoiceplatform.master.adminmenu.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import jakarta.persistence.Column;
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
 * Admin Menu Entity.
 *
 * <p>
 * Represents a menu/page assigned to a specific ADMIN
 * (client/tenant).
 * </p>
 *
 * <p>
 * This is application-specific and intentionally does not
 * belong to the reusable Infinitio common library.
 * </p>
 *
 * <p>
 * Relationship:
 *
 * <pre>
 * AdminMenu
 *    |
 *    +-- Admin/Tenant
 *    |
 *    +-- Menu
 * </pre>
 *
 * </p>
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
        name = "admin_menus",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_admin_menu",
                        columnNames = {
                                "admin_id",
                                "menu_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_admin_menus_admin",
                        columnList = "admin_id"
                ),
                @Index(
                        name = "idx_admin_menus_menu",
                        columnList = "menu_id"
                ),
                @Index(
                        name = "idx_admin_menus_active",
                        columnList = "is_active"
                ),
                @Index(
                        name = "idx_admin_menus_deleted",
                        columnList = "is_deleted"
                ),
                @Index(
                        name = "idx_admin_menus_visible",
                        columnList = "is_visible"
                )
        }
)
public class AdminMenu extends BaseEntity {

    /**
     * ADMIN/client receiving the menu access.
     *
     * <p>
     * The actual database entity is Tenant because the current
     * AI Voice application represents a client using Tenant.
     * </p>
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "admin_id",
            nullable = false
    )
    private Tenant admin;

    /**
     * Menu assigned to the ADMIN.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "menu_id",
            nullable = false
    )
    private Menu menu;

    /**
     * Indicates whether the menu is visible.
     *
     * 1 = Visible
     * 0 = Hidden
     */
    @Column(
            name = "is_visible",
            nullable = false
    )
    private Integer isVisible;
}