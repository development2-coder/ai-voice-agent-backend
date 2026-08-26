package com.infinitio.aivoiceplatform.organization.organizationstatus.repository;

import com.infinitio.aivoiceplatform.organization.organizationstatus.entity.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Organization Status Repository.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface OrganizationStatusRepository
        extends JpaRepository<OrganizationStatus, Long> {

    Optional<OrganizationStatus> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Optional<OrganizationStatus>
    findByOrganizationStatusCodeAndIsDeleted(
            String organizationStatusCode,
            Integer isDeleted
    );

    Optional<OrganizationStatus>
    findByOrganizationStatusNameAndIsDeleted(
            String organizationStatusName,
            Integer isDeleted
    );

    Page<OrganizationStatus> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    boolean existsByOrganizationStatusCodeAndIsDeleted(
            String organizationStatusCode,
            Integer isDeleted
    );

    boolean existsByOrganizationStatusNameAndIsDeleted(
            String organizationStatusName,
            Integer isDeleted
    );

    boolean
    existsByOrganizationStatusCodeAndIsDeletedAndPublicIdNot(
            String organizationStatusCode,
            Integer isDeleted,
            String publicId
    );

    boolean
    existsByOrganizationStatusNameAndIsDeletedAndPublicIdNot(
            String organizationStatusName,
            Integer isDeleted,
            String publicId
    );
}