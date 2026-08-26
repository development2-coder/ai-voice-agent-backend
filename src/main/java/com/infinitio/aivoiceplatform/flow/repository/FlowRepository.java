package com.infinitio.aivoiceplatform.flow.repository;

import com.infinitio.aivoiceplatform.flow.entity.Flow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}