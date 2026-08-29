package com.infinitio.aivoiceplatform.llm.repository;

import com.infinitio.aivoiceplatform.llm.entity.LlmInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LlmInteractionRepository
        extends JpaRepository<LlmInteraction, Long> {

    Page<LlmInteraction>
    findByCallPublicIdOrderByCreatedAtAsc(
            String callPublicId,
            Pageable pageable
    );
}