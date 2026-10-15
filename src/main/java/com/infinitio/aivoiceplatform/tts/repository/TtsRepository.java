package com.infinitio.aivoiceplatform.tts.repository;

import com.infinitio.aivoiceplatform.tts.entity.Tts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for TTS.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsRepository
        extends JpaRepository<Tts, Long> {

    /**
     * Finds TTS by public ID.
     *
     * @param publicId public ID
     * @return TTS
     */
    Optional<Tts> findByPublicId(
            String publicId
    );

    /**
     * Finds a non-deleted TTS by public ID.
     *
     * @param publicId public ID
     * @param isDeleted deleted flag
     * @return TTS
     */
    Optional<Tts> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Finds all non-deleted TTS configurations globally.
     *
     * <p>
     * Used by SUPER_ADMIN.
     * </p>
     *
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated TTS configurations
     */
    Page<Tts> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Finds TTS configurations belonging to a tenant.
     *
     * <p>
     * The tenant is resolved through:
     *
     * <pre>
     * Tts → Agent → Tenant
     * </pre>
     *
     * @param tenantId tenant ID
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated tenant TTS configurations
     */
    Page<Tts> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Checks whether a TTS code already exists.
     *
     * @param ttsCode TTS code
     * @return true if code exists
     */
    boolean existsByTtsCode(
            String ttsCode
    );

    /**
     * Checks whether a TTS name already exists.
     *
     * @param ttsName TTS name
     * @return true if name exists
     */
    boolean existsByTtsName(
            String ttsName
    );
}