package com.infinitio.aivoiceplatform.master.role.repository;

import com.infinitio.aivoiceplatform.master.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository
        extends JpaRepository<Role, Long> {

    Optional<Role> findByPublicId(
            String publicId
    );

    Optional<Role> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    boolean existsByRoleCode(
            String roleCode
    );

    boolean existsByRoleName(
            String roleName
    );

    boolean existsByRoleCodeAndIsDeleted(
            String roleCode,
            Integer isDeleted
    );

    boolean existsByRoleNameAndIsDeleted(
            String roleName,
            Integer isDeleted
    );

    boolean existsByRoleCodeAndIsDeletedAndPublicIdNot(
            String roleCode,
            Integer isDeleted,
            String publicId
    );

    boolean existsByRoleNameAndIsDeletedAndPublicIdNot(
            String roleName,
            Integer isDeleted,
            String publicId
    );

    Page<Role> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );
}