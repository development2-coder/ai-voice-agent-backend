package com.infinitio.aivoiceplatform.organization.organizationtype.repository;

import com.infinitio.aivoiceplatform.organization.organizationtype.entity.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Organization Type Repository.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface OrganizationTypeRepository
        extends JpaRepository<OrganizationType, Long> {

    Optional<OrganizationType> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Optional<OrganizationType> findByOrganizationTypeCodeAndIsDeleted(
            String organizationTypeCode,
            Integer isDeleted
    );

    Optional<OrganizationType> findByOrganizationTypeNameAndIsDeleted(
            String organizationTypeName,
            Integer isDeleted
    );

    Page<OrganizationType> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    boolean existsByOrganizationTypeCodeAndIsDeleted(
            String organizationTypeCode,
            Integer isDeleted
    );

    boolean existsByOrganizationTypeNameAndIsDeleted(
            String organizationTypeName,
            Integer isDeleted
    );

    boolean existsByOrganizationTypeCodeAndIsDeletedAndPublicIdNot(
            String organizationTypeCode,
            Integer isDeleted,
            String publicId
    );

    boolean existsByOrganizationTypeNameAndIsDeletedAndPublicIdNot(
            String organizationTypeName,
            Integer isDeleted,
            String publicId
    );
}