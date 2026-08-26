package com.infinitio.aivoiceplatform.organization.tenant.repository;

import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Tenant Repository.
 *
 * Handles persistence operations for Tenant.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TenantRepository
        extends JpaRepository<Tenant, Long>,
        JpaSpecificationExecutor<Tenant> {

    Optional<Tenant> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Optional<Tenant> findByTenantCodeAndIsDeleted(
            String tenantCode,
            Integer isDeleted
    );

    boolean existsByTenantCodeAndIsDeleted(
            String tenantCode,
            Integer isDeleted
    );

    boolean existsBySubdomainAndIsDeleted(
            String subdomain,
            Integer isDeleted
    );

    Optional<Tenant> findBySubdomainAndIsDeleted(
            String subdomain,
            Integer isDeleted
    );
}