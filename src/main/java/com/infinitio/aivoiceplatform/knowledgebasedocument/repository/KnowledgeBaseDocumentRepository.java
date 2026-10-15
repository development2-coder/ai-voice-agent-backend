package com.infinitio.aivoiceplatform.knowledgebasedocument.repository;

import com.infinitio.aivoiceplatform.knowledgebasedocument.entity.KnowledgeBaseDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Knowledge Base Documents.
 */
public interface KnowledgeBaseDocumentRepository
        extends JpaRepository<KnowledgeBaseDocument, Long> {

    Optional<KnowledgeBaseDocument> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    List<KnowledgeBaseDocument> findByIsDeleted(
            Integer isDeleted
    );

    List<KnowledgeBaseDocument>
    findByKnowledgeBase_Agent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted
    );

    boolean existsByDocumentCode(String documentCode);

    boolean existsByDocumentName(String documentName);
}