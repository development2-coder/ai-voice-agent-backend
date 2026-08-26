package com.infinitio.aivoiceplatform.master.rolemenu.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
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
 * Role Menu Entity.
 *
 * Represents menu access assigned to a Role.
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
        name = "role_menus",

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_menu",
                        columnNames = {
                                "role_id",
                                "menu_id"
                        }
                )
        },

        indexes = {
                @Index(
                        name = "idx_role_menus_role",
                        columnList = "role_id"
                ),

                @Index(
                        name = "idx_role_menus_menu",
                        columnList = "menu_id"
                ),

                @Index(
                        name = "idx_role_menus_active",
                        columnList = "is_active"
                ),

                @Index(
                        name = "idx_role_menus_deleted",
                        columnList = "is_deleted"
                ),

                @Index(
                        name = "idx_role_menus_visible",
                        columnList = "is_visible"
                )
        }
)
public class RoleMenu extends BaseEntity {

    /**
     * Role receiving the menu access.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;


    /**
     * Menu assigned to the role.
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
     * Menu visibility for this role.
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