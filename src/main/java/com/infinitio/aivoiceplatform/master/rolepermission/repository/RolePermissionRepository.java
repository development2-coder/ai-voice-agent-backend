package com.infinitio.aivoiceplatform.master.rolepermission.repository;

import com.infinitio.aivoiceplatform.master.rolepermission.entity.RolePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

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

    @Query("""
    SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END
    FROM RolePermission rp
    JOIN rp.role role
    JOIN rp.permission permission
    WHERE role.roleCode = :roleCode
      AND role.isActive = 1
      AND role.isDeleted = 0
      AND permission.permissionCode = :permissionCode
      AND permission.isActive = 1
      AND permission.isDeleted = 0
      AND rp.isActive = 1
      AND rp.isDeleted = 0
""")
    boolean existsActivePermissionByRoleCodeAndPermissionCode(
            @Param("roleCode") String roleCode,
            @Param("permissionCode") String permissionCode
    );

    /**
     * Finds all role-permission mappings, including soft-deleted mappings,
     * for reuse during assignment updates.
     *
     * @param roleId role database ID
     * @return existing mappings for the role
     */
    List<RolePermission>
    findByRole_IdOrderByPermission_PermissionNameAsc(
            Long roleId
    );

    /**
     * Finds active, non-deleted permissions assigned to a role.
     *
     * @param roleId role database ID
     * @param isActive active flag
     * @param isDeleted deleted flag
     * @return assigned permissions
     */
    List<RolePermission>
    findByRole_IdAndIsActiveAndIsDeletedOrderByPermission_PermissionNameAsc(
            Long roleId,
            Integer isActive,
            Integer isDeleted
    );

    /**
     * Finds all non-deleted role-permission mappings without pagination.
     *
     * @param isDeleted deleted flag
     * @return role-permission mappings
     */
    List<RolePermission>
    findByIsDeletedOrderByRole_RoleNameAscPermission_PermissionNameAsc(
            Integer isDeleted
    );
}