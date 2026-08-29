package com.infinitio.aivoiceplatform.transcript.repository;

import com.infinitio.aivoiceplatform.transcript.entity.TranscriptArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranscriptArtifactRepository
        extends JpaRepository<TranscriptArtifact, Long> {

    Optional<TranscriptArtifact>
    findTopByCallPublicIdOrderByCreatedAtDesc(
            String callPublicId
    );
}