package com.infinitio.aivoiceplatform.llm.repository;

import com.infinitio.aivoiceplatform.llm.entity.Llm;
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
}