package com.infinitio.aivoiceplatform.master.usermenu.repository;

import com.infinitio.aivoiceplatform.master.usermenu.entity.UserMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Menu Repository.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface UserMenuRepository
        extends JpaRepository<UserMenu, Long> {

    Optional<UserMenu> findByPublicId(
            String publicId
    );

    Optional<UserMenu> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    boolean existsByUserIdAndMenuIdAndIsDeleted(
            Long userId,
            Long menuId,
            Integer isDeleted
    );

    boolean existsByUserIdAndMenuIdAndIsDeletedAndIdNot(
            Long userId,
            Long menuId,
            Integer isDeleted,
            Long id
    );

    Page<UserMenu> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    List<UserMenu>
    findByUserIdAndIsActiveAndIsDeletedAndIsVisibleOrderByMenuDisplayOrderAsc(
            Long userId,
            Integer isActive,
            Integer isDeleted,
            Integer isVisible
    );

    Page<UserMenu> findByUser_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );
}