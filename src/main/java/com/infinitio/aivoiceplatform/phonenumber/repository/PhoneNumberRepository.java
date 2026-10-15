package com.infinitio.aivoiceplatform.phonenumber.repository;

import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for phone-number persistence and tenant-scoped queries.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface PhoneNumberRepository
        extends JpaRepository<PhoneNumber, Long> {

    /**
     * Lists non-deleted phone numbers across all tenants.
     *
     * @param isDeleted soft-delete flag
     * @param pageable pagination settings
     * @return phone-number page
     */
    Page<PhoneNumber> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Lists non-deleted phone numbers belonging to a tenant.
     *
     * @param tenantId tenant database ID
     * @param isDeleted soft-delete flag
     * @param pageable pagination settings
     * @return phone-number page for the tenant
     */
    Page<PhoneNumber> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Finds a phone number by public ID.
     *
     * @param publicId public identifier
     * @return matching phone number, if present
     */
    Optional<PhoneNumber> findByPublicId(String publicId);

    /**
     * Finds a phone number by public ID and deletion status.
     *
     * @param publicId public identifier
     * @param isDeleted soft-delete flag
     * @return matching phone number, if present
     */
    Optional<PhoneNumber> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Checks whether the phone number already exists.
     *
     * @param phoneNumber phone number
     * @return true if it exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Checks whether the provider number ID already exists.
     *
     * @param providerNumberId provider number identifier
     * @return true if it exists
     */
    boolean existsByProviderNumberId(String providerNumberId);
}