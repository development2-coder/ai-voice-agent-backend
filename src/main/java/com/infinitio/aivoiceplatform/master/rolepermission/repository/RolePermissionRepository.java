package com.infinitio.aivoiceplatform.master.rolepermission.repository;

import com.infinitio.aivoiceplatform.master.rolepermission.entity.RolePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role Permission mappings.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface RolePermissionRepository
        extends JpaRepository<RolePermission, Long> {

    // =========================================================
    // FIND
    // =========================================================

    Optional<RolePermission> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // DUPLICATE VALIDATION
    // =========================================================

    boolean existsByRole_IdAndPermission_IdAndIsDeleted(
            Long roleId,
            Long permissionId,
            Integer isDeleted
    );


    boolean existsByRole_IdAndPermission_IdAndIsDeletedAndIdNot(
            Long roleId,
            Long permissionId,
            Integer isDeleted,
            Long id
    );


    // =========================================================
    // PAGINATION
    // =========================================================

    Page<RolePermission> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );
}