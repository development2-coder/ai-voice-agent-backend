package com.infinitio.aivoiceplatform.master.adminmenu.repository;

import com.infinitio.aivoiceplatform.master.adminmenu.entity.AdminMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Admin Menu.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface AdminMenuRepository
        extends JpaRepository<AdminMenu, Long> {

    /**
     * Finds AdminMenu by public ID.
     *
     * @param publicId public ID
     * @return AdminMenu
     */
    Optional<AdminMenu> findByPublicId(
            String publicId
    );

    /**
     * Finds non-deleted AdminMenu by public ID.
     *
     * @param publicId public ID
     * @param isDeleted deleted flag
     * @return AdminMenu
     */
    Optional<AdminMenu> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Checks whether a menu is already assigned to an ADMIN.
     *
     * @param adminId admin/tenant ID
     * @param menuId menu ID
     * @param isDeleted deleted flag
     * @return true when assignment exists
     */
    boolean existsByAdmin_IdAndMenu_IdAndIsDeleted(
            Long adminId,
            Long menuId,
            Integer isDeleted
    );

    /**
     * Checks duplicate assignment excluding a record.
     *
     * @param adminId admin/tenant ID
     * @param menuId menu ID
     * @param isDeleted deleted flag
     * @param id current record ID
     * @return true when assignment exists
     */
    boolean existsByAdmin_IdAndMenu_IdAndIsDeletedAndIdNot(
            Long adminId,
            Long menuId,
            Integer isDeleted,
            Long id
    );

    /**
     * Gets active visible menus assigned to an ADMIN.
     *
     * @param adminId admin/tenant ID
     * @param isActive active flag
     * @param isDeleted deleted flag
     * @param isVisible visible flag
     * @return assigned menus
     */
    List<AdminMenu>
    findByAdmin_IdAndIsActiveAndIsDeletedAndIsVisibleOrderByMenu_DisplayOrderAsc(
            Long adminId,
            Integer isActive,
            Integer isDeleted,
            Integer isVisible
    );

    /**
     * Gets all AdminMenu records for an ADMIN.
     *
     * @param adminId admin/tenant ID
     * @param isDeleted deleted flag
     * @param pageable pagination
     * @return page
     */
    Page<AdminMenu> findByAdmin_IdAndIsDeleted(
            Long adminId,
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Gets all AdminMenu records.
     *
     * <p>
     * Used by SUPER_ADMIN.
     * </p>
     *
     * @param isDeleted deleted flag
     * @param pageable pagination
     * @return page
     */
    Page<AdminMenu> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );
}