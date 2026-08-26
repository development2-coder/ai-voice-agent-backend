package com.infinitio.aivoiceplatform.organization.organizationaddress.repository;

import com.infinitio.aivoiceplatform.organization.organizationaddress.entity.OrganizationAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationAddressRepository
        extends JpaRepository<OrganizationAddress, Long> {

    Optional<OrganizationAddress> findByPublicId(String publicId);

    Optional<OrganizationAddress> findByOrganization_PublicId(String publicId);

    boolean existsByOrganization_PublicId(String publicId);

}