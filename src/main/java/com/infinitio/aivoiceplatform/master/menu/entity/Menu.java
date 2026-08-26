package com.infinitio.aivoiceplatform.master.menu.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
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

/**
 * Menu Entity.
 *
 * Represents a menu item available in the AI Voice Platform.
 *
 * A Menu:
 * - belongs to one Platform Module
 * - may optionally have a parent Menu
 * - can be system or custom
 * - can be activated/deactivated
 * - supports soft deletion through BaseEntity
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
        name = "menus",
        indexes = {

                @Index(
                        name = "idx_menus_module_id",
                        columnList = "module_id"
                ),

                @Index(
                        name = "idx_menus_parent_menu_id",
                        columnList = "parent_menu_id"
                ),

                @Index(
                        name = "idx_menus_is_active",
                        columnList = "is_active"
                ),

                @Index(
                        name = "idx_menus_is_deleted",
                        columnList = "is_deleted"
                ),

                @Index(
                        name = "idx_menus_display_order",
                        columnList = "display_order"
                )
        }
)
public class Menu extends BaseEntity {


    // =========================================================
    // PLATFORM MODULE
    // =========================================================

    /**
     * Platform module to which this menu belongs.
     *
     * Example:
     *
     * AI Agents
     *     ├── Agent Management
     *     └── Agent Configuration
     *
     * AI Dialer
     *     ├── Dialer
     *     └── Campaigns
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "module_id",
            nullable = false
    )
    private PlatformModule module;


    // =========================================================
    // PARENT MENU
    // =========================================================

    /**
     * Parent menu.
     *
     * Null means this is a top-level menu.
     *
     * Example:
     *
     * AI Agents
     *     ├── Agent List
     *     ├── Create Agent
     *     └── Agent Settings
     */
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "parent_menu_id"
    )
    private Menu parentMenu;


    // =========================================================
    // MENU CODE
    // =========================================================

    /**
     * Unique technical menu code.
     *
     * Example:
     *
     * USER_MANAGEMENT
     * AGENT_MANAGEMENT
     * AI_DIALER
     */
    @Column(
            name = "menu_code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String menuCode;


    // =========================================================
    // MENU NAME
    // =========================================================

    /**
     * Human-readable menu name.
     */
    @Column(
            name = "menu_name",
            nullable = false,
            length = 150
    )
    private String menuName;


    // =========================================================
    // ROUTE
    // =========================================================

    /**
     * Frontend route associated with the menu.
     *
     * Example:
     *
     * /users
     * /agents
     * /dialer
     *
     * Route may be null for parent/group menus
     * that do not directly navigate to a page.
     */
    @Column(
            name = "route",
            length = 255
    )
    private String route;


    // =========================================================
    // ICON
    // =========================================================

    /**
     * Frontend icon identifier.
     */
    @Column(
            name = "icon",
            length = 255
    )
    private String icon;


    // =========================================================
    // DISPLAY ORDER
    // =========================================================

    /**
     * Display order within the module/parent menu.
     */
    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;


    // =========================================================
    // SYSTEM MENU
    // =========================================================

    /**
     * Indicates whether the menu is a system menu.
     *
     * 1 = System menu
     * 0 = Custom menu
     */
    @Column(
            name = "is_system",
            nullable = false
    )
    private Integer isSystem;
}