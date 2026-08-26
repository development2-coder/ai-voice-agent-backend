package com.infinitio.aivoiceplatform.organization.organizationbranding.repository;

import com.infinitio.aivoiceplatform.organization.organizationbranding.entity.OrganizationBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationBrandingRepository
        extends JpaRepository<OrganizationBranding, Long> {

    Optional<OrganizationBranding> findByPublicId(String publicId);

    Optional<OrganizationBranding> findByOrganization_PublicId(
            String publicId);

    boolean existsByOrganization_PublicId(String publicId);

}