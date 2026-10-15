package com.infinitio.aivoiceplatform.voice.repository;

import com.infinitio.aivoiceplatform.voice.entity.Voice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Voice.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface VoiceRepository
        extends JpaRepository<Voice, Long> {

    Optional<Voice> findByPublicId(String publicId);

    boolean existsByVoiceCode(String voiceCode);

    boolean existsByVoiceName(String voiceName);

    Optional<Voice> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Optional<Voice> findByAgent_Tenant_IdAndPublicIdAndIsDeleted(
            Long tenantId,
            String publicId,
            Integer isDeleted
    );

    Page<Voice> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );

    Page<Voice> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    boolean existsByVoiceCodeAndAgent_Tenant_IdAndIsDeleted(
            String voiceCode,
            Long tenantId,
            Integer isDeleted
    );

    boolean existsByVoiceNameAndAgent_Tenant_IdAndIsDeleted(
            String voiceName,
            Long tenantId,
            Integer isDeleted
    );
}