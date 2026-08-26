package com.infinitio.aivoiceplatform.master.menu.repository;

import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Menu entity.
 *
 * Handles database operations for Menu.
 *
 * Soft deleted menus are excluded wherever applicable.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface MenuRepository
        extends JpaRepository<Menu, Long>,
        MenuRepositoryCustom {


    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    /**
     * Find a non-deleted menu by public ID.
     *
     * @param publicId menu public UUID
     * @param isDeleted deleted flag
     * @return menu if found
     */
    Optional<Menu> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY MENU CODE
    // =========================================================

    /**
     * Find a non-deleted menu by menu code.
     *
     * @param menuCode menu code
     * @param isDeleted deleted flag
     * @return menu if found
     */
    Optional<Menu> findByMenuCodeAndIsDeleted(
            String menuCode,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY MENU NAME
    // =========================================================

    /**
     * Find a non-deleted menu by menu name.
     *
     * @param menuName menu name
     * @param isDeleted deleted flag
     * @return menu if found
     */
    Optional<Menu> findByMenuNameAndIsDeleted(
            String menuName,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - MENU CODE
    // =========================================================

    /**
     * Check whether a non-deleted menu exists
     * with the supplied menu code.
     *
     * @param menuCode menu code
     * @param isDeleted deleted flag
     * @return true if exists
     */
    boolean existsByMenuCodeAndIsDeleted(
            String menuCode,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - MENU NAME
    // =========================================================

    /**
     * Check whether a non-deleted menu exists
     * with the supplied menu name.
     *
     * @param menuName menu name
     * @param isDeleted deleted flag
     * @return true if exists
     */
    boolean existsByMenuNameAndIsDeleted(
            String menuName,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - MENU CODE EXCLUDING CURRENT MENU
    // =========================================================

    /**
     * Check whether another non-deleted menu has
     * the supplied menu code.
     *
     * Used during update validation.
     *
     * @param menuCode menu code
     * @param isDeleted deleted flag
     * @param publicId current menu public ID
     * @return true if another menu exists
     */
    boolean existsByMenuCodeAndIsDeletedAndPublicIdNot(
            String menuCode,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // EXISTS - MENU NAME EXCLUDING CURRENT MENU
    // =========================================================

    /**
     * Check whether another non-deleted menu has
     * the supplied menu name.
     *
     * Used during update validation.
     *
     * @param menuName menu name
     * @param isDeleted deleted flag
     * @param publicId current menu public ID
     * @return true if another menu exists
     */
    boolean existsByMenuNameAndIsDeletedAndPublicIdNot(
            String menuName,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // PAGINATION - NON-DELETED
    // =========================================================

    /**
     * Find all non-deleted menus using pagination.
     *
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated menus
     */
    Page<Menu> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );


    // =========================================================
    // ACTIVE + NON-DELETED MENUS
    // =========================================================

    /**
     * Find active and non-deleted menus.
     *
     * @param isActive active flag
     * @param isDeleted deleted flag
     * @return active menus
     */
    List<Menu>
    findByIsActiveAndIsDeletedOrderByDisplayOrderAsc(
            Integer isActive,
            Integer isDeleted
    );


    // =========================================================
    // MODULE MENUS
    // =========================================================

    /**
     * Find all non-deleted menus belonging to
     * a particular Platform Module.
     *
     * @param moduleId module database ID
     * @param isDeleted deleted flag
     * @return module menus
     */
    List<Menu>
    findByModule_IdAndIsDeletedOrderByDisplayOrderAsc(
            Long moduleId,
            Integer isDeleted
    );


    // =========================================================
    // MODULE ACTIVE MENUS
    // =========================================================

    /**
     * Find active and non-deleted menus belonging
     * to a particular Platform Module.
     *
     * @param moduleId module database ID
     * @param isActive active flag
     * @param isDeleted deleted flag
     * @return active module menus
     */
    List<Menu>
    findByModule_IdAndIsActiveAndIsDeletedOrderByDisplayOrderAsc(
            Long moduleId,
            Integer isActive,
            Integer isDeleted
    );


    // =========================================================
    // CHILD MENUS
    // =========================================================

    /**
     * Find non-deleted child menus of a parent menu.
     *
     * @param parentMenuId parent menu database ID
     * @param isDeleted deleted flag
     * @return child menus
     */
    List<Menu>
    findByParentMenu_IdAndIsDeletedOrderByDisplayOrderAsc(
            Long parentMenuId,
            Integer isDeleted
    );


    // =========================================================
    // ACTIVE CHILD MENUS
    // =========================================================

    /**
     * Find active and non-deleted child menus.
     *
     * @param parentMenuId parent menu database ID
     * @param isActive active flag
     * @param isDeleted deleted flag
     * @return active child menus
     */
    List<Menu>
    findByParentMenu_IdAndIsActiveAndIsDeletedOrderByDisplayOrderAsc(
            Long parentMenuId,
            Integer isActive,
            Integer isDeleted
    );
}