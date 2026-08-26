package com.infinitio.aivoiceplatform.knowledgebase.repository;

import com.infinitio.aivoiceplatform.knowledgebase.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Knowledge Base.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface KnowledgeBaseRepository
        extends JpaRepository<KnowledgeBase, Long> {

    Optional<KnowledgeBase> findByPublicId(
            String publicId
    );

    boolean existsByKnowledgeBaseCode(
            String knowledgeBaseCode
    );

    boolean existsByKnowledgeBaseName(
            String knowledgeBaseName
    );
}