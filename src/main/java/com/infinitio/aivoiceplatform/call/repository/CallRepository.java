package com.infinitio.aivoiceplatform.call.repository;

import com.infinitio.aivoiceplatform.call.entity.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * @param pageable pagination information
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
     * <p>
     * The explicit JPQL query is used so the provider identifier
     * mapping is unambiguous during Voice Gateway call resolution.
     * </p>
     *
     * @param providerCallId provider supplied call identifier
     * @return matching call
     */
    @Query("""
            SELECT call
            FROM Call call
            WHERE call.providerCallId = :providerCallId
            """)
    Optional<Call> findByProviderCallId(
            @Param("providerCallId") String providerCallId
    );

    /**
     * Finds a call using a native database comparison.
     *
     * <p>
     * This method is intended for Voice Gateway diagnostics and
     * can be used to verify that the application datasource can
     * directly resolve the provider identifier from the calls table.
     * </p>
     *
     * @param providerCallId provider supplied call identifier
     * @return matching call
     */
    @Query(
            value = """
                    SELECT *
                    FROM calls
                    WHERE provider_call_id = :providerCallId
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<Call> findByProviderCallIdNative(
            @Param("providerCallId") String providerCallId
    );
}