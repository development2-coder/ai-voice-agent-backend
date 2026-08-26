package com.infinitio.aivoiceplatform.master.platformmodule.repository;

import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Platform Module.
 *
 * Handles database operations for PlatformModule.
 *
 * Soft deleted records are excluded wherever applicable.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface PlatformModuleRepository
        extends JpaRepository<PlatformModule, Long> {


    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    /**
     * Find non-deleted platform module by public ID.
     *
     * @param publicId Public UUID
     * @param isDeleted Deleted flag
     * @return PlatformModule if found
     */
    Optional<PlatformModule> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY MODULE CODE
    // =========================================================

    /**
     * Find non-deleted platform module by module code.
     *
     * @param moduleCode Module code
     * @param isDeleted Deleted flag
     * @return PlatformModule if found
     */
    Optional<PlatformModule> findByModuleCodeAndIsDeleted(
            String moduleCode,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY MODULE NAME
    // =========================================================

    /**
     * Find non-deleted platform module by module name.
     *
     * @param moduleName Module name
     * @param isDeleted Deleted flag
     * @return PlatformModule if found
     */
    Optional<PlatformModule> findByModuleNameAndIsDeleted(
            String moduleName,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY ROUTE
    // =========================================================

    /**
     * Find non-deleted platform module by route.
     *
     * @param route Module route
     * @param isDeleted Deleted flag
     * @return PlatformModule if found
     */
    Optional<PlatformModule> findByRouteAndIsDeleted(
            String route,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - MODULE CODE
    // =========================================================

    /**
     * Check whether a non-deleted module exists
     * with the given module code.
     *
     * @param moduleCode Module code
     * @param isDeleted Deleted flag
     * @return true if exists
     */
    boolean existsByModuleCodeAndIsDeleted(
            String moduleCode,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - MODULE NAME
    // =========================================================

    /**
     * Check whether a non-deleted module exists
     * with the given module name.
     *
     * @param moduleName Module name
     * @param isDeleted Deleted flag
     * @return true if exists
     */
    boolean existsByModuleNameAndIsDeleted(
            String moduleName,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS - ROUTE
    // =========================================================

    /**
     * Check whether a non-deleted module exists
     * with the given route.
     *
     * @param route Module route
     * @param isDeleted Deleted flag
     * @return true if exists
     */
    boolean existsByRouteAndIsDeleted(
            String route,
            Integer isDeleted
    );


    // =========================================================
    // FIND ACTIVE MODULES
    // =========================================================

    /**
     * Find all active and non-deleted modules.
     *
     * Uses the actual entity property name:
     *
     * isActive
     *
     * instead of:
     *
     * active
     *
     * Results are ordered by display order.
     *
     * @param isActive Active flag
     * @param isDeleted Deleted flag
     * @return active platform modules
     */
    List<PlatformModule>
    findByIsActiveAndIsDeletedOrderByDisplayOrderAsc(
            Integer isActive,
            Integer isDeleted
    );


    // =========================================================
    // FIND ALL NON-DELETED
    // =========================================================

    /**
     * Find all non-deleted platform modules with pagination.
     *
     * @param isDeleted Deleted flag
     * @param pageable pagination information
     * @return paginated platform modules
     */
    Page<PlatformModule> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );


    // =========================================================
    // FIND ALL NON-DELETED ORDERED
    // =========================================================

    /**
     * Find all non-deleted platform modules ordered
     * by display order.
     *
     * @param isDeleted Deleted flag
     * @return platform modules
     */
    List<PlatformModule>
    findByIsDeletedOrderByDisplayOrderAsc(
            Integer isDeleted
    );
}