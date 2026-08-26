package com.infinitio.aivoiceplatform.agentconfig.repository;

import com.infinitio.aivoiceplatform.agentconfig.entity.AgentConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Agent Configuration Repository.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface AgentConfigRepository
        extends JpaRepository<AgentConfig, Long> {

    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    Optional<AgentConfig> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY AGENT
    // =========================================================

    Optional<AgentConfig> findByAgentIdAndIsDeleted(
            Long agentId,
            Integer isDeleted
    );


    // =========================================================
    // EXISTS BY AGENT
    // =========================================================

    boolean existsByAgentId(
            Long agentId
    );


    // =========================================================
    // PAGINATION
    // =========================================================

    Page<AgentConfig> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );
}