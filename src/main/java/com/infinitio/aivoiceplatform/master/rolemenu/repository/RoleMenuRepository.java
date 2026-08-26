package com.infinitio.aivoiceplatform.master.rolemenu.repository;

import com.infinitio.aivoiceplatform.master.rolemenu.entity.RoleMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}