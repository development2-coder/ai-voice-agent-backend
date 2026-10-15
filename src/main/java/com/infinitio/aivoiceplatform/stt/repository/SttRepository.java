package com.infinitio.aivoiceplatform.stt.repository;

import com.infinitio.aivoiceplatform.stt.entity.Stt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for STT.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttRepository
        extends JpaRepository<Stt, Long> {

    Optional<Stt> findByPublicId(String publicId);

    Optional<Stt> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Optional<Stt> findByAgent_Tenant_IdAndPublicIdAndIsDeleted(
            Long tenantId,
            String publicId,
            Integer isDeleted
    );

    Page<Stt> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    Page<Stt> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );

    boolean existsBySttCode(String sttCode);

    boolean existsBySttName(String sttName);

    boolean existsBySttCodeAndIsDeleted(
            String sttCode,
            Integer isDeleted
    );

    boolean existsBySttNameAndIsDeleted(
            String sttName,
            Integer isDeleted
    );
}