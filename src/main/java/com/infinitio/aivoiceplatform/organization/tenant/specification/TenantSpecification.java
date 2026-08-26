package com.infinitio.aivoiceplatform.organization.tenant.specification;

import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import org.springframework.data.jpa.domain.Specification;

/**
 * Tenant Specifications.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class TenantSpecification {

    private static final Integer NOT_DELETED = 0;

    private TenantSpecification() {
    }

    public static Specification<Tenant> isNotDeleted() {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("isDeleted"),
                        NOT_DELETED
                );
    }
}