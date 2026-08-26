package com.infinitio.aivoiceplatform.knowledgebasedocument.repository;

import com.infinitio.aivoiceplatform.knowledgebasedocument.entity.KnowledgeBaseDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Knowledge Base Document.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface KnowledgeBaseDocumentRepository
        extends JpaRepository<KnowledgeBaseDocument, Long> {

    Optional<KnowledgeBaseDocument> findByPublicId(
            String publicId
    );

    boolean existsByDocumentCode(
            String documentCode
    );

    boolean existsByDocumentName(
            String documentName
    );
}