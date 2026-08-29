package com.infinitio.aivoiceplatform.stt.repository;

import com.infinitio.aivoiceplatform.stt.entity.SttInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SttInteractionRepository
        extends JpaRepository<SttInteraction, Long> {

    Page<SttInteraction>
    findByCallPublicIdOrderByCreatedAtAsc(
            String callPublicId,
            Pageable pageable
    );
}