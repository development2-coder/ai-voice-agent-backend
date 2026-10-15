package com.infinitio.aivoiceplatform.master.usermenu.entity;

import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.user.entity.User;
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
 * User Menu Entity.
 *
 * <p>
 * Represents a menu/page explicitly assigned to an individual user.
 * </p>
 *
 * <p>
 * User-level menu access is used so that an ADMIN can control
 * which pages are accessible to users belonging to the ADMIN's
 * own tenant without modifying the global USER role.
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
        name = "user_menus",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_menu",
                        columnNames = {
                                "user_id",
                                "menu_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_menus_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_menus_menu",
                        columnList = "menu_id"
                ),
                @Index(
                        name = "idx_user_menus_active",
                        columnList = "is_active"
                ),
                @Index(
                        name = "idx_user_menus_deleted",
                        columnList = "is_deleted"
                ),
                @Index(
                        name = "idx_user_menus_visible",
                        columnList = "is_visible"
                )
        }
)
public class UserMenu extends BaseEntity {

    /**
     * User receiving menu access.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * Menu assigned to the user.
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