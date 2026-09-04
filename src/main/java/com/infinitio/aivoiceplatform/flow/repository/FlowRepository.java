package com.infinitio.aivoiceplatform.flow.repository;

import com.infinitio.aivoiceplatform.flow.entity.Flow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.Optional;

public interface FlowRepository
        extends JpaRepository<Flow, Long> {

    Optional<Flow> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    Page<Flow> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    boolean existsByAgentIdAndNameAndIsDeleted(
            Long agentId,
            String name,
            Integer isDeleted
    );

    boolean existsByAgentIdAndNameAndIsDeletedAndPublicIdNot(
            Long agentId,
            String name,
            Integer isDeleted,
            String publicId
    );

    /**
     * Finds the active Flow belonging to an Agent.
     *
     * @param agentId Agent database identifier
     * @param isDeleted soft-delete flag
     * @return active Flow
     */
    Optional<Flow> findFirstByAgentIdAndIsDeletedOrderByVersionDesc(
            Long agentId,
            Integer isDeleted
    );

    List<Flow> findAllByAgentIdAndIsDeleted(
            Long agentId,
            Integer isDeleted
    );
}