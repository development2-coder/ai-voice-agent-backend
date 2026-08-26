package com.infinitio.aivoiceplatform.master.permission.repository;

import com.infinitio.aivoiceplatform.master.permission.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository
        extends JpaRepository<Permission, Long> {

    // =========================================================
    // FIND
    // =========================================================

    Optional<Permission> findByPublicId(
            String publicId
    );

    Optional<Permission> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Optional<Permission> findByPermissionCode(
            String permissionCode
    );


    // =========================================================
    // CREATE VALIDATION
    // =========================================================

    boolean existsByPermissionCode(
            String permissionCode
    );

    boolean existsByPermissionName(
            String permissionName
    );

    boolean existsByPermissionCodeAndIsDeleted(
            String permissionCode,
            Integer isDeleted
    );

    boolean existsByPermissionNameAndIsDeleted(
            String permissionName,
            Integer isDeleted
    );


    // =========================================================
    // UPDATE VALIDATION
    // =========================================================

    boolean existsByPermissionCodeAndIsDeletedAndPublicIdNot(
            String permissionCode,
            Integer isDeleted,
            String publicId
    );

    boolean existsByPermissionNameAndIsDeletedAndPublicIdNot(
            String permissionName,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // PAGINATION
    // =========================================================

    Page<Permission> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );
}