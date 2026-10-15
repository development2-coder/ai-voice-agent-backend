package com.infinitio.aivoiceplatform.knowledgebase.repository;

import com.infinitio.aivoiceplatform.knowledgebase.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseRepository
        extends JpaRepository<KnowledgeBase, Long> {

    Optional<KnowledgeBase> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    List<KnowledgeBase> findByIsDeleted(
            Integer isDeleted
    );

    List<KnowledgeBase> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted
    );

    boolean existsByKnowledgeBaseCode(
            String knowledgeBaseCode
    );

    boolean existsByKnowledgeBaseName(
            String knowledgeBaseName
    );
}