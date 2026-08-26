package com.infinitio.aivoiceplatform.organization.organization.repository;

import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Organization Repository.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface OrganizationRepository
        extends JpaRepository<Organization, Long> {

    Optional<Organization> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Optional<Organization> findByOrganizationCodeAndIsDeleted(
            String organizationCode,
            Integer isDeleted
    );

    Optional<Organization> findByEmailAndIsDeleted(
            String email,
            Integer isDeleted
    );

    boolean existsByOrganizationCodeAndIsDeleted(
            String organizationCode,
            Integer isDeleted
    );

    boolean existsByEmailAndIsDeleted(
            String email,
            Integer isDeleted
    );

    boolean existsByOrganizationCodeAndIsDeletedAndPublicIdNot(
            String organizationCode,
            Integer isDeleted,
            String publicId
    );

    boolean existsByEmailAndIsDeletedAndPublicIdNot(
            String email,
            Integer isDeleted,
            String publicId
    );

    Page<Organization> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );
}