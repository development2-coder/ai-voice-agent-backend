package com.infinitio.aivoiceplatform.master.rolemenu.repository;

import com.infinitio.aivoiceplatform.master.rolemenu.entity.RoleMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role Menu Repository.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface RoleMenuRepository
        extends JpaRepository<RoleMenu, Long> {


    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    Optional<RoleMenu> findByPublicId(
            String publicId
    );


    Optional<RoleMenu> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // DUPLICATE VALIDATION
    // =========================================================

    boolean existsByRoleIdAndMenuIdAndIsDeleted(
            Long roleId,
            Long menuId,
            Integer isDeleted
    );


    boolean existsByRoleIdAndMenuIdAndIsDeletedAndIdNot(
            Long roleId,
            Long menuId,
            Integer isDeleted,
            Long id
    );


    // =========================================================
    // PAGINATION
    // =========================================================

    Page<RoleMenu> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );


    // =========================================================
    // ACTIVE ROLE MENUS
    // =========================================================

    List<RoleMenu>
    findByRoleIdAndIsActiveAndIsDeletedAndIsVisibleOrderByMenuDisplayOrderAsc(
            Long roleId,
            Integer isActive,
            Integer isDeleted,
            Integer isVisible
    );

    @Query("""
        SELECT CASE WHEN COUNT(rm) > 0 THEN true ELSE false END
        FROM RoleMenu rm
        JOIN rm.role role
        JOIN rm.menu menu
        WHERE role.id = :roleId
          AND menu.id = :menuId
          AND rm.isActive = :isActive
          AND rm.isDeleted = :isDeleted
          AND rm.isVisible = :isVisible
          AND role.isActive = :isActive
          AND role.isDeleted = :isDeleted
          AND menu.isActive = :isActive
          AND menu.isDeleted = :isDeleted
        """)
    boolean existsActiveVisibleMenuForRole(
            @Param("roleId") Long roleId,
            @Param("menuId") Long menuId,
            @Param("isActive") Integer isActive,
            @Param("isDeleted") Integer isDeleted,
            @Param("isVisible") Integer isVisible
    );


    /**
     * Finds non-deleted menu assignments for a selected role,
     * ordered by menu display order.
     *
     * @param roleId role database ID
     * @param isDeleted deleted flag
     * @return assigned role menus
     */
    List<RoleMenu>
    findByRole_IdAndIsDeletedOrderByMenu_DisplayOrderAsc(
            Long roleId,
            Integer isDeleted
    );

    /**
     * Finds all non-deleted role-menu assignments without pagination.
     *
     * @param isDeleted deleted flag
     * @return all role-menu assignments
     */
    List<RoleMenu>
    findByIsDeletedOrderByRole_RoleNameAscMenu_DisplayOrderAsc(
            Integer isDeleted
    );
}