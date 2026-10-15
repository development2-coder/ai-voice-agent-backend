package com.infinitio.aivoiceplatform.master.userpermission.repository;

import com.infinitio.aivoiceplatform.master.userpermission.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for individual user permission assignments.
 */
@Repository
public interface UserPermissionRepository
        extends JpaRepository<UserPermission, Long> {

    /**
     * Finds an assignment for a user and permission, including inactive
     * or soft-deleted assignments so they can be reactivated.
     */
    Optional<UserPermission> findByUser_IdAndPermission_Id(
            Long userId,
            Long permissionId
    );

    /**
     * Returns all assignments for a user, including inactive assignments.
     */
    List<UserPermission> findByUser_Id(Long userId);

    /**
     * Checks whether an individual user has an active permission.
     */
    @Query("""
            SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END
            FROM UserPermission up
            JOIN up.user u
            JOIN up.permission p
            WHERE u.id = :userId
              AND u.isActive = 1
              AND u.isDeleted = 0
              AND p.permissionCode = :permissionCode
              AND p.isActive = 1
              AND p.isDeleted = 0
              AND up.isActive = 1
              AND up.isDeleted = 0
            """)
    boolean existsActivePermissionForUser(
            @Param("userId") Long userId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * Lists active permissions assigned directly to a user.
     */
    @Query("""
            SELECT up
            FROM UserPermission up
            JOIN FETCH up.permission p
            WHERE up.user.id = :userId
              AND up.isActive = 1
              AND up.isDeleted = 0
              AND p.isActive = 1
              AND p.isDeleted = 0
            ORDER BY p.displayOrder ASC, p.permissionName ASC
            """)
    List<UserPermission> findActiveAssignmentsForUser(
            @Param("userId") Long userId
    );
}