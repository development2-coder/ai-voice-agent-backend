package com.infinitio.aivoiceplatform.llm.repository;

import com.infinitio.aivoiceplatform.llm.entity.Llm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for LLM.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface LlmRepository
        extends JpaRepository<Llm, Long> {

    Optional<Llm> findByPublicId(String publicId);

    boolean existsByLlmCode(String llmCode);

    boolean existsByLlmName(String llmName);

    Optional<Llm> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Page<Llm> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    Page<Llm> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );
}