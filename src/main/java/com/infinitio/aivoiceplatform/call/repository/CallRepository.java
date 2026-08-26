package com.infinitio.aivoiceplatform.call.repository;

import com.infinitio.aivoiceplatform.call.entity.Call;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Call persistence operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallRepository
        extends JpaRepository<Call, Long> {

    /**
     * Finds a call using its public identifier.
     *
     * @param publicId public identifier
     * @return matching call
     */
    Optional<Call> findByPublicId(
            String publicId
    );

    /**
     * Checks whether a call exists using its public identifier.
     *
     * @param publicId public identifier
     * @return true when call exists
     */
    boolean existsByPublicId(
            String publicId
    );

    /**
     * Checks whether a provider call identifier already exists.
     *
     * @param providerCallId provider call identifier
     * @return true when provider call identifier exists
     */
    boolean existsByProviderCallId(
            String providerCallId
    );

    /**
     * Checks whether a provider call identifier exists
     * for another call.
     *
     * <p>
     * Used during call update validation to prevent duplicate
     * provider call identifiers.
     * </p>
     *
     * @param providerCallId provider call identifier
     * @param id current call database identifier
     * @return true when another call has the provider call identifier
     */
    boolean existsByProviderCallIdAndIdNot(
            String providerCallId,
            Long id
    );

    /**
     * Finds a non-deleted call using its public identifier.
     *
     * @param publicId public identifier
     * @param isDeleted deleted flag
     * @return matching call
     */
    Optional<Call> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Finds calls belonging to a tenant.
     *
     * @param tenantId tenant database identifier
     * @param pageable pagination information
     * @return paginated calls
     */
//    Page<Call> findByTenantId(
//            Long tenantId,
//            Pageable pageable
//    );

    /**
     * Finds the latest flow execution associated with a call.
     *
     * <p>
     * NOTE:
     * This method does NOT belong to CallRepository.
     * It should remain in FlowExecutionRepository.
     * </p>
     */

    /**
     * Finds calls by deleted status.
     *
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated calls
     */
    Page<Call> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Finds calls associated with a campaign contact
     * and deleted status.
     *
     * @param campaignContactId campaign contact database identifier
     * @param isDeleted deleted flag
     * @return matching calls
     */
    Page<Call> findByCampaignContactIdAndIsDeleted(
            Long campaignContactId,
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Finds a call using its provider call identifier.
     *
     * @param providerCallId provider call identifier
     * @return matching call
     */
    Optional<Call> findByProviderCallId(
            String providerCallId
    );
}