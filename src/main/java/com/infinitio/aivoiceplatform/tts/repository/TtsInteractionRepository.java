package com.infinitio.aivoiceplatform.tts.repository;

import com.infinitio.aivoiceplatform.tts.entity.TtsInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TtsInteractionRepository
        extends JpaRepository<TtsInteraction, Long> {

    Page<TtsInteraction>
    findByCallPublicIdOrderByCreatedAtAsc(
            String callPublicId,
            Pageable pageable
    );
}